package aethereal.module.movement;

import aethereal.core.*;
import aethereal.core.Module;
import aethereal.event.TickEvent;
import aethereal.module.combat.Aura;
import aethereal.handler.InventoryHandler;
import aethereal.module.combat.AuraUtil;
import aethereal.setting.BooleanSetting;
import aethereal.setting.SliderSetting;
import aethereal.util.CounterUtil;
import aethereal.util.InventoryUtil;
import aethereal.util.Rotation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

@ModuleRegister(name = "Elytra Target", description = "Aims at an enemy while flying with elytra and boosts with a firework from the hotbar without moving it into your hand", category = Category.Movement)
public class ElytraTarget extends Module {
    // Ported from the other client: fly IN FRONT of the target (predicted path, latency compensated) and turn to
    // the hitbox right before each hit.
    private final BooleanSetting frontMode = new BooleanSetting("Front of target", true);
    private final SliderSetting frontDistance = new SliderSetting("Front distance", 3.0f, 1.0f, 15.0f, 0.5f).a(() -> {
        return this.frontMode.c();
    });
    private final BooleanSetting hitboxAim = new BooleanSetting("Hitbox aim", true).a(() -> {
        return this.frontMode.c();
    });
    private final BooleanSetting flyOff = new BooleanSetting("Fly off", true);
    private final SliderSetting flyAway = new SliderSetting("Fly away", 8.0f, 5.0f, 40.0f, 1.0f).a(() -> {
        return this.flyOff.c();
    });
    private final BooleanSetting flyOffLowHp = new BooleanSetting("Fly off on low health", true).a(() -> {
        return this.flyOff.c();
    });
    private final BooleanSetting flyOffUsing = new BooleanSetting("Fly off when using an item", true).a(() -> {
        return this.flyOff.c();
    });
    private final BooleanSetting cutOff = new BooleanSetting("Cut off fleeing target", true);
    private final SliderSetting fleeSpeed = new SliderSetting("Flee min speed", 0.15f, 0.05f, 1.0f, 0.05f).a(() -> {
        return this.cutOff.c();
    });
    private final SliderSetting cutOffLead = new SliderSetting("Cut-off lead", 4.0f, 0.0f, 12.0f, 0.5f).a(() -> {
        return this.cutOff.c();
    });
    private final SliderSetting maxPredict = new SliderSetting("Max prediction ticks", 12.0f, 1.0f, 30.0f, 1.0f).a(() -> {
        return this.cutOff.c();
    });
    private final BooleanSetting hitAlign = new BooleanSetting("Hit alignment", true);
    private final SliderSetting alignRange = new SliderSetting("Alignment range", 4.5f, 2.0f, 8.0f, 0.5f).a(() -> {
        return this.hitAlign.c();
    });
    private final SliderSetting hitReach = new SliderSetting("Hit reach", 3.2f, 2.0f, 6.0f, 0.1f).a(() -> {
        return this.hitAlign.c();
    });
    private final BooleanSetting precise = new BooleanSetting("Precise pursuit", true);
    private final BooleanSetting drift = new BooleanSetting("Drift compensation", true);
    private final SliderSetting driftStrength = new SliderSetting("Drift strength", 1.0f, 0.0f, 3.0f, 0.1f).a(() -> {
        return this.drift.c();
    });
    private final SliderSetting overshoot = new SliderSetting("Overshoot distance", 0.0f, 0.0f, 8.0f, 0.5f).a(() -> {
        return !this.precise.c();
    });
    private final SliderSetting liftHeight = new SliderSetting("Lift height", 1.0f, 0.0f, 6.0f, 0.5f);
    private final BooleanSetting keepLock = new BooleanSetting("Keep lock after hit", true);
    private final BooleanSetting critPitch = new BooleanSetting("Crit pitch limit", true);
    private final SliderSetting turnSpeed = new SliderSetting("Turn speed", 180.0f, 30.0f, 360.0f, 10.0f);
    private final SliderSetting fireworkDelay = new SliderSetting("Firework delay (ms)", 150.0f, 0.0f, 1000.0f, 10.0f);
    private final SliderSetting chaseDelay = new SliderSetting("Chase firework delay (ms)", 0.0f, 0.0f, 1000.0f, 10.0f);
    private final BooleanSetting limitBySpeed = new BooleanSetting("Limit firework by speed", false);
    private final SliderSetting fireworkSpeedLimit = new SliderSetting("Firework speed limit", 1.5f, 0.5f, 3.0f, 0.1f).a(() -> {
        return this.limitBySpeed.c();
    });

    private final CounterUtil b = new CounterUtil();
    private boolean swapped;
    private int slot = -1;
    private int wait;
    private boolean chasing;

    // Target motion tracking (other players' client velocity is unreliable, so we measure position change per tick).
    private LivingEntity lastTarget;
    private Vec3d lastTargetPos;
    private Vec3d targetVelocity = Vec3d.ZERO;

    // Front-of-target smoothing state
    private LivingEntity frontEntity;
    private Vec3d smMotion = Vec3d.ZERO;
    private Vec3d smDir;
    private Vec3d smCenter;
    private boolean smMoving;
    private boolean smFast;
    private Vec3d prevRawDir;
    private double smOmega;
    private Vec3d compPos;

    public ElytraTarget() {
        a(this.frontMode, this.frontDistance, this.hitboxAim, this.flyOff, this.flyAway, this.flyOffLowHp, this.flyOffUsing,
                this.cutOff, this.fleeSpeed, this.cutOffLead, this.maxPredict, this.hitAlign, this.alignRange, this.hitReach,
                this.precise, this.drift, this.driftStrength, this.overshoot, this.liftHeight, this.keepLock, this.critPitch,
                this.turnSpeed, this.fireworkDelay, this.chaseDelay, this.limitBySpeed, this.fireworkSpeedLimit);
    }

    @Override
    public void c() {
        super.c();
        this.lastTarget = null;
        this.lastTargetPos = null;
        this.targetVelocity = Vec3d.ZERO;
        this.swapped = false;
        this.slot = -1;
        this.chasing = false;
        this.frontEntity = null;
        this.smDir = null;
        this.smCenter = null;
        this.smMoving = false;
        this.smFast = false;
        this.prevRawDir = null;
        this.smOmega = 0.0d;
        this.compPos = null;
        this.b.b();
    }

    @EventTarget
    public void a(TickEvent event) {
        Aura aura = Reaxc.getInstance().getModuleProcessor().t().B();
        InventoryHandler inventory = Reaxc.getInstance().getModuleProcessor().v().getInventoryHandler();
        if (mc.player.isGliding()) {
            boolean totemBusy = Reaxc.getInstance().getModuleProcessor().t().V().b;
            // Fires every "Firework delay" ms (or the chase delay while chasing), speed limit is opt-in.
            boolean shouldFire = this.b.a((long) (this.chasing ? this.chaseDelay.c().floatValue() : this.fireworkDelay.c().floatValue()))
                    && (!this.limitBySpeed.c().booleanValue() || mc.player.getVelocity().length() < (double) this.fireworkSpeedLimit.c().floatValue());
            if (this.swapped) {
                if (mc.player.getOffHandStack().getItem() == Items.FIREWORK_ROCKET) {
                    mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
                    this.b.b();
                    inventory.moveItem(this.slot, 45, 1);
                    this.swapped = false;
                    this.slot = -1;
                } else {
                    this.wait++;
                    if (this.wait > 10) {
                        this.swapped = false;
                        this.slot = -1;
                    }
                }
            } else if (shouldFire) {
                if (mc.player.getOffHandStack().getItem() == Items.FIREWORK_ROCKET) {
                    mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
                    this.b.b();
                } else {
                    // Firework in the hotbar: use it silently (slot switched only on the server for one use).
                    int hotbar = InventoryUtil.a(Items.FIREWORK_ROCKET, true, false);
                    int current = mc.player.getInventory().selectedSlot;
                    if (hotbar != -1) {
                        if (hotbar != current) {
                            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(hotbar));
                            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(current));
                        } else {
                            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        }
                        this.b.b();
                    } else if (!totemBusy && inventory.a().isEmpty()) {
                        // Firework only in the inventory: fall back to the off-hand swap.
                        int found = InventoryUtil.b(Items.FIREWORK_ROCKET);
                        if (found != -1) {
                            inventory.moveItem(found, 45, 1);
                            this.slot = found;
                            this.swapped = true;
                            this.wait = 0;
                        }
                    }
                }
            }
            if (!aura.m()) {
                this.chasing = false;
                return;
            }
            LivingEntity target = aura.getTarget();
            if (target == null) {
                this.chasing = false;
                this.lastTarget = null;
                this.lastTargetPos = null;
                this.targetVelocity = Vec3d.ZERO;
                return;
            }
            trackTarget(target);

            Vec3d eye = mc.player.getEyePos();
            Vec3d enemy = target.getBoundingBox().getCenter();
            double dx = enemy.x - eye.x;
            double dz = enemy.z - eye.z;
            double horizontal = Math.sqrt((dx * dx) + (dz * dz));
            double nx = horizontal == 0.0d ? 0.0d : dx / horizontal;
            double nz = horizontal == 0.0d ? 0.0d : dz / horizontal;
            boolean fleeing = this.cutOff.c().booleanValue() && isFleeing(dx, dz, horizontal);

            if (this.frontMode.c().booleanValue()) {
                double dist3d = eye.distanceTo(enemy);
                this.chasing = fleeing || dist3d > 8.0d;
                Vec3d frontAim;
                if (shouldLeave()) {
                    frontAim = flyOffPoint(enemy, eye);
                } else {
                    frontAim = aimPointFor(target, computeFrontCenter(target));
                }
                Rotation frontRotation = Rotation.a(eye, frontAim);
                float frontYaw = AuraUtil.a(mc.player.getYaw(), frontRotation.c(), 1.0f);
                float frontPitch = critPitch(AuraUtil.a(mc.player.getPitch(), frontRotation.d(), 1.0f));
                Reaxc.getInstance().getModuleProcessor().k().startAiming(new Rotation(frontYaw, frontPitch), this.turnSpeed.c().floatValue(), 1, 1);
                return;
            }

            // Hit alignment: when close, look at a real point of the hitbox instead of the "fly through" point.
            // NOTE: RotationProcessor.priority is a static that is never lowered, so everything here uses priority 1.
            if (this.hitAlign.c().booleanValue() && (this.keepLock.c().booleanValue() || aura.attackCooldown >= 6)
                    && Math.sqrt(AuraUtil.a(eye, target)) <= (double) (fleeing ? Math.min(this.alignRange.c().floatValue(), this.hitReach.c().floatValue()) : this.alignRange.c().floatValue())) {
                Vec3d rel = AuraUtil.a(eye, target, (double) this.hitReach.c().floatValue(), true);
                Vec3d point = rel.equals(Vec3d.ZERO) ? enemy : eye.add(rel);
                if (fleeing) {
                    point = point.add(this.targetVelocity.x, 0.0d, this.targetVelocity.z);
                }
                Rotation hitRotation = Rotation.a(eye, point);
                float hitYaw = AuraUtil.a(mc.player.getYaw(), hitRotation.c(), 1.0f);
                float hitPitch = critPitch(AuraUtil.a(mc.player.getPitch(), hitRotation.d(), 1.0f));
                Reaxc.getInstance().getModuleProcessor().k().startAiming(new Rotation(hitYaw, hitPitch), this.turnSpeed.c().floatValue(), 1, 1);
                return;
            }

            double lift = Math.max(0.0d, (double) this.liftHeight.c().floatValue() - q());
            double mySpeed = Math.max(mc.player.getVelocity().length(), 0.8d);
            double distance = eye.distanceTo(enemy);
            this.chasing = fleeing || distance > 8.0d;
            Vec3d aim;
            if (this.precise.c().booleanValue() || fleeing) {
                // Lead pursuit: aim where the target will be when we get there; when it runs, aim further ahead.
                double ticks = MathHelper.clamp(distance / mySpeed, 1.0d, (double) this.maxPredict.c().floatValue());
                double vy = target.isGliding() ? MathHelper.clamp(this.targetVelocity.y, -1.0d, 1.0d) : 0.0d;
                double hx = this.targetVelocity.x;
                double hz = this.targetVelocity.z;
                double hLen = Math.sqrt((hx * hx) + (hz * hz));
                double lead = fleeing ? (double) this.cutOffLead.c().floatValue() : 0.0d;
                double ux = hLen == 0.0d ? 0.0d : hx / hLen;
                double uz = hLen == 0.0d ? 0.0d : hz / hLen;
                aim = new Vec3d(enemy.x + (hx * ticks) + (ux * lead), enemy.y + (vy * ticks) + lift, enemy.z + (hz * ticks) + (uz * lead));
            } else {
                double over = (double) this.overshoot.c().floatValue();
                aim = new Vec3d(enemy.x + (nx * over), enemy.y + lift, enemy.z + (nz * over));
            }
            if (this.drift.c().booleanValue()) {
                // Elytra velocity lags behind where you look; push the look direction against the drift.
                Vec3d velocity = mc.player.getVelocity();
                Vec3d to = aim.subtract(eye);
                double len = to.length();
                if (len > 3.0d && velocity.length() > 0.3d) {
                    Vec3d dir = to.multiply(1.0d / len);
                    Vec3d vdir = velocity.normalize();
                    Vec3d fixed = dir.add(dir.subtract(vdir).multiply((double) this.driftStrength.c().floatValue()));
                    if (fixed.length() > 0.001d) {
                        aim = eye.add(fixed.normalize().multiply(len));
                    }
                }
            }
            Rotation aimRotation = Rotation.a(eye, aim);
            boolean level = !this.keepLock.c().booleanValue() && aura.attackCooldown <= 3;
            float Yaw = AuraUtil.a(mc.player.getYaw(), aimRotation.c(), 1.0f);
            float Pitch = critPitch(AuraUtil.a(mc.player.getPitch(), level ? 0.0f : aimRotation.d(), level ? 1.0f : Math.clamp(aura.attackCooldown / 10.0f, 0.0f, 1.0f)));
            Reaxc.getInstance().getModuleProcessor().k().startAiming(new Rotation(Yaw, Pitch), this.turnSpeed.c().floatValue(), 1, 1);
        }
    }

    // A crit needs fallDistance > 0 on the server; while gliding it stays 1.0 only if you are not diving too steeply.
    private float critPitch(float pitch) {
        if (this.critPitch.c().booleanValue() && mc.player.getVelocity().y < -0.4d) {
            return Math.min(pitch, 0.0f);
        }
        return pitch;
    }

    // ---------------------------------------------------------------- front-of-target (ported)

    private boolean shouldLeave() {
        boolean lowHp = this.flyOffLowHp.c().booleanValue() && mc.player.getHealth() <= 4.0f;
        boolean using = this.flyOffUsing.c().booleanValue() && mc.player.isUsingItem() && !mc.player.getActiveItem().isOf(Items.SHIELD);
        return this.flyOff.c().booleanValue() && (lowHp || using);
    }

    private Vec3d flyOffPoint(Vec3d enemy, Vec3d eye) {
        Vec3d away = new Vec3d(eye.x - enemy.x, 0.0d, eye.z - enemy.z);
        if (away.lengthSquared() < 1.0E-4d) {
            away = Vec3d.fromPolar(0.0f, mc.player.getYaw());
        }
        away = away.normalize().multiply((double) this.flyAway.c().floatValue());
        Vec3d point = new Vec3d(enemy.x + away.x, Math.max(enemy.y, eye.y) + 6.0d, enemy.z + away.z);
        for (int i = 0; i < 3 && isObstructed(eye, point); i++) {
            point = point.add(0.0d, 6.0d, 0.0d);
        }
        return point;
    }

    private boolean isObstructed(Vec3d from, Vec3d to) {
        return mc.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player)).getType() != HitResult.Type.MISS;
    }

    private static Vec3d lerpVec(Vec3d a, Vec3d b, double t) {
        return a.add(b.subtract(a).multiply(t));
    }

    // The target we see is behind its real position (network + interpolation); shift predictions forward by this.
    private double latencyTicks() {
        double ping = 0.0d;
        if (mc.getNetworkHandler() != null && mc.player != null) {
            net.minecraft.client.network.PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) {
                ping = entry.getLatency();
            }
        }
        return MathHelper.clamp(1.5d + ping / 50.0d, 1.5d, 7.0d);
    }

    // Position after "steps" ticks at constant speed and constant turn rate om (rad/tick); om = 0 is a straight line.
    private Vec3d predictArc(Vec3d pos0, Vec3d motion, double om, int steps) {
        double c = Math.cos(om);
        double s = Math.sin(om);
        double lc = Math.cos(om * 1.5d);
        double ls = Math.sin(om * 1.5d);
        double hx = (motion.x * lc) - (motion.z * ls);
        double hz = (motion.x * ls) + (motion.z * lc);
        double px = pos0.x;
        double py = pos0.y;
        double pz = pos0.z;
        for (int i = 0; i < steps; i++) {
            double nx = (hx * c) - (hz * s);
            double nz = (hx * s) + (hz * c);
            hx = nx;
            hz = nz;
            px += hx;
            pz += hz;
            py += motion.y;
        }
        return new Vec3d(px, py, pz);
    }

    // Hitbox centre in front of the target's movement/look direction at "Front distance", smoothed between ticks.
    private Vec3d computeFrontCenter(LivingEntity base) {
        Vec3d motion = new Vec3d(base.getX() - base.prevX, base.getY() - base.prevY, base.getZ() - base.prevZ);

        if (base != this.frontEntity) {
            this.frontEntity = base;
            this.smMotion = motion;
            this.smDir = null;
            this.smCenter = null;
            this.smMoving = false;
            this.smFast = false;
            this.prevRawDir = null;
            this.smOmega = 0.0d;
            this.compPos = null;
        }

        this.smMotion = lerpVec(this.smMotion, motion, motion.lengthSquared() > this.smMotion.lengthSquared() * 1.5d ? 0.6d : 0.35d);
        double speedSq = this.smMotion.lengthSquared();
        this.smMoving = this.smMoving ? speedSq > 0.01d : speedSq > 0.03d;
        this.smFast = this.smMoving;

        Vec3d dir;
        if (this.smMoving) {
            dir = this.smMotion.normalize();
        } else {
            Vec3d look = base.getRotationVecClient();
            Vec3d flat = new Vec3d(look.x, 0.0d, look.z);
            dir = flat.lengthSquared() < 1.0E-4d ? Vec3d.fromPolar(0.0f, base.getYaw()) : flat.normalize();
        }

        // Horizontal turn rate of the target (circles around us -> arc, straight -> ~0)
        if (!this.smMoving) {
            this.smOmega *= 0.5d;
            this.prevRawDir = null;
        } else {
            if (this.prevRawDir != null && Math.hypot(this.prevRawDir.x, this.prevRawDir.z) > 1.0E-4d && Math.hypot(dir.x, dir.z) > 1.0E-4d) {
                double cr = (this.prevRawDir.x * dir.z) - (this.prevRawDir.z * dir.x);
                double dt = (this.prevRawDir.x * dir.x) + (this.prevRawDir.z * dir.z);
                this.smOmega += (Math.atan2(cr, dt) - this.smOmega) * 0.4d;
            }
            this.prevRawDir = dir;
        }

        if (this.smDir == null) {
            this.smDir = dir;
        } else {
            double follow = this.smDir.dotProduct(dir) > 0.7d ? 0.3d : 0.7d;
            follow = Math.max(follow, MathHelper.clamp(0.3d + Math.abs(this.smOmega) * 2.0d, 0.3d, 0.9d));
            Vec3d blended = lerpVec(this.smDir, dir, follow);
            this.smDir = blended.lengthSquared() < 1.0E-6d ? dir : blended.normalize();
        }

        Vec3d leadPos = base.getPos();
        Vec3d heading = this.smDir;
        this.compPos = base.getPos();
        if (this.smMoving && !mc.player.isTouchingWater()) {
            double mySpeed = Math.max(mc.player.getVelocity().length(), 1.0d);
            Vec3d me = mc.player.getPos();
            double om = Math.abs(this.smOmega) < 0.02d ? 0.0d : MathHelper.clamp(this.smOmega, -0.8d, 0.8d);
            int lagSteps = (int) Math.round(latencyTicks());
            this.compPos = predictArc(base.getPos(), this.smMotion, om, lagSteps);

            int steps = 0;
            for (int i = 0; i < 3; i++) {
                Vec3d predI = predictArc(base.getPos(), this.smMotion, om, lagSteps + steps);
                steps = (int) Math.round(MathHelper.clamp(me.distanceTo(predI) / mySpeed, 0.0d, 12.0d));
            }
            Vec3d pred = predictArc(base.getPos(), this.smMotion, om, lagSteps + steps);
            Vec3d off = pred.subtract(base.getPos());
            if (off.lengthSquared() > 196.0d) {
                off = off.normalize().multiply(14.0d);
            }
            leadPos = base.getPos().add(off);

            double ang = om * (lagSteps + steps + 1.5d);
            double ca = Math.cos(ang);
            double sa = Math.sin(ang);
            Vec3d rot = new Vec3d((this.smDir.x * ca) - (this.smDir.z * sa), this.smDir.y, (this.smDir.x * sa) + (this.smDir.z * ca));
            heading = rot.lengthSquared() < 1.0E-6d ? this.smDir : rot.normalize();
        }

        Vec3d half = new Vec3d(0.0d, base.getHeight() / 2.0d, 0.0d);
        Vec3d origin = leadPos.add(half);

        // Keep the point inside attack range so the hit can still land
        double maxHitDist = Math.max(1.0d, (double) this.hitReach.c().floatValue() - 0.5d);
        double dist = Math.min((double) this.frontDistance.c().floatValue(), maxHitDist);
        Vec3d center = origin.add(heading.multiply(dist));
        for (int i = 0; i < 3 && isObstructed(origin, center); i++) {
            dist *= 0.6d;
            center = origin.add(heading.multiply(dist));
        }
        if (dist < 1.0d) {
            dist = 1.0d;
            center = origin.add(heading.multiply(dist));
        }

        // If the point ended up inside a block, lift it
        for (int i = 0; i < 4; i++) {
            BlockPos bp = BlockPos.ofFloored(center);
            if (mc.world.getBlockState(bp).getCollisionShape(mc.world, bp).isEmpty()) {
                break;
            }
            center = center.add(0.0d, 1.0d, 0.0d);
        }

        if (this.smCenter == null) {
            this.smCenter = center;
        } else {
            double dd = this.smCenter.distanceTo(center);
            if (dd > 6.0d) {
                this.smCenter = center;
            } else if (dd > 0.3d) {
                this.smCenter = lerpVec(this.smCenter, center, 0.45d);
            }
        }
        return this.smCenter;
    }

    // True when the hit is (almost) ready and the target is close: turn to the target so the hit lands on it.
    private boolean inHitWindow(LivingEntity base) {
        double radius = (double) this.hitReach.c().floatValue();
        double closing = mc.player.getVelocity().subtract(this.smMotion).length();
        double margin = MathHelper.clamp(1.0d + closing * 1.2d, 1.0d, 4.5d);
        boolean inRange = Math.sqrt(AuraUtil.a(base)) <= radius + margin;
        boolean ready = mc.player.getMainHandStack().isOf(Items.MACE) || mc.player.getAttackCooldownProgress(0.0f) >= 0.7f;
        return ready && inRange;
    }

    // Would looking at "aim" put the crosshair through the target's body?
    private boolean rayHitsTarget(LivingEntity base, Vec3d aim) {
        Vec3d eye = mc.player.getEyePos();
        Vec3d dir = aim.subtract(eye);
        double lenSq = dir.lengthSquared();
        if (lenSq < 1.0E-6d) {
            return true;
        }
        Vec3d tc = (this.compPos != null ? this.compPos : base.getPos()).add(0.0d, base.getHeight() * 0.5d, 0.0d);
        double t = tc.subtract(eye).dotProduct(dir) / lenSq;
        if (t < 0.0d) {
            return false;
        }
        Vec3d closest = eye.add(dir.multiply(t));
        Vec3d diff = closest.subtract(tc);
        double horizontal = Math.hypot(diff.x, diff.z);
        return horizontal <= base.getWidth() * 0.5d + 0.3d && Math.abs(diff.y) <= base.getHeight() * 0.5d + 0.2d;
    }

    // The packet leaves after we have moved this tick, so subtract our own velocity from the aim.
    private Vec3d hitAimPoint(LivingEntity base) {
        Vec3d center = (this.compPos != null ? this.compPos : base.getPos()).add(0.0d, base.getHeight() * 0.5d, 0.0d).add(this.smMotion);
        Vec3d eye = mc.player.getEyePos();
        Vec3d a = center.subtract(mc.player.getVelocity());
        Vec3d toA = a.subtract(eye);
        if (toA.lengthSquared() < 0.25d || toA.dotProduct(center.subtract(eye)) <= 0.0d) {
            return center;
        }
        return a;
    }

    private Vec3d aimPointFor(LivingEntity base, Vec3d front) {
        Vec3d targetAim = hitAimPoint(base);
        if (!this.smFast) {
            return targetAim;
        }
        Vec3d aim = stableAimPoint(front);
        if (inHitWindow(base) && (this.hitboxAim.c().booleanValue() || !rayHitsTarget(base, aim))) {
            return targetAim;
        }
        return aim;
    }

    // Close to the front point the direction would snap around; slide the aim slightly past it instead.
    private Vec3d stableAimPoint(Vec3d front) {
        if (this.smDir == null) {
            return front;
        }
        double d = mc.player.getEyePos().distanceTo(front);
        if (d >= 3.0d) {
            return front;
        }
        double w = MathHelper.clamp((d - 0.8d) / 2.2d, 0.0d, 1.0d);
        Vec3d beyond = front.add(this.smDir.multiply(4.0d));
        return lerpVec(beyond, front, w);
    }

    private void trackTarget(LivingEntity target) {
        Vec3d pos = target.getPos();
        if (target != this.lastTarget || this.lastTargetPos == null) {
            this.targetVelocity = Vec3d.ZERO;
        } else {
            Vec3d raw = pos.subtract(this.lastTargetPos);
            this.targetVelocity = this.targetVelocity.multiply(0.5d).add(raw.multiply(0.5d));
        }
        this.lastTarget = target;
        this.lastTargetPos = pos;
    }

    private boolean isFleeing(double dx, double dz, double horizontal) {
        double hx = this.targetVelocity.x;
        double hz = this.targetVelocity.z;
        double speed = Math.sqrt((hx * hx) + (hz * hz));
        if (speed < (double) this.fleeSpeed.c().floatValue() || horizontal < 0.001d) {
            return false;
        }
        double cos = ((hx * dx) + (hz * dz)) / (speed * horizontal);
        return cos > -0.3d;
    }

    private double q() {
        Vec3d start = mc.player.getPos();
        Vec3d end = start.subtract(0.0d, 2.0d, 0.0d);
        BlockHitResult hit = mc.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
        if (hit.getType() == HitResult.Type.MISS) {
            return 2.0d;
        }
        return start.getY() - hit.getPos().y;
    }
}
