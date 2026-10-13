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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

@ModuleRegister(name = "Elytra Target", description = "Aims at an enemy while flying with elytra and boosts with a firework from the hotbar without moving it into your hand", category = Category.Movement)
public class ElytraTarget extends Module {
    // Defaults below match the old hard-coded behaviour, so nothing changes unless you touch a setting.
    private final BooleanSetting cutOff = new BooleanSetting("Cut off fleeing target", true);
    private final SliderSetting fleeSpeed = new SliderSetting("Flee min speed", 0.15f, 0.05f, 1.0f, 0.05f).a(() -> {
        return this.cutOff.c();
    });
    private final SliderSetting cutOffLead = new SliderSetting("Cut-off lead", 2.0f, 0.0f, 8.0f, 0.5f).a(() -> {
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
    private final SliderSetting overshoot = new SliderSetting("Overshoot distance", 4.0f, 0.0f, 8.0f, 0.5f);
    private final SliderSetting liftHeight = new SliderSetting("Lift height", 3.0f, 0.0f, 6.0f, 0.5f);
    private final SliderSetting turnSpeed = new SliderSetting("Turn speed", 180.0f, 30.0f, 360.0f, 10.0f);
    private final SliderSetting fireworkDelay = new SliderSetting("Firework delay (ms)", 150.0f, 50.0f, 1000.0f, 10.0f);
    private final SliderSetting fireworkSpeedLimit = new SliderSetting("Firework speed limit", 1.5f, 0.5f, 3.0f, 0.1f);

    private final CounterUtil b = new CounterUtil();
    private boolean swapped;
    private int slot = -1;
    private int wait;

    // Target motion tracking (other players' client velocity is unreliable, so we measure position change per tick).
    private LivingEntity lastTarget;
    private Vec3d lastTargetPos;
    private Vec3d targetVelocity = Vec3d.ZERO;

    public ElytraTarget() {
        a(this.cutOff, this.fleeSpeed, this.cutOffLead, this.maxPredict, this.hitAlign, this.alignRange, this.hitReach,
                this.overshoot, this.liftHeight, this.turnSpeed, this.fireworkDelay, this.fireworkSpeedLimit);
    }

    @Override
    public void c() {
        super.c();
        this.lastTarget = null;
        this.lastTargetPos = null;
        this.targetVelocity = Vec3d.ZERO;
        this.swapped = false;
        this.slot = -1;
    }

    @EventTarget
    public void a(TickEvent event) {
        Aura aura = Reaxc.getInstance().getModuleProcessor().t().B();
        InventoryHandler inventory = Reaxc.getInstance().getModuleProcessor().v().getInventoryHandler();
        if (aura.m() && mc.player.isGliding()) {
            // The sword stays in the main hand; the firework is never held visibly.
            boolean totemBusy = Reaxc.getInstance().getModuleProcessor().t().V().b;
            boolean shouldFire = (this.b.a((long) this.fireworkDelay.c().floatValue()) && mc.player.getVelocity().length() < (double) this.fireworkSpeedLimit.c().floatValue()) || aura.attackCooldown == 1;
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
            } else if (!totemBusy && shouldFire) {
                if (mc.player.getOffHandStack().getItem() == Items.FIREWORK_ROCKET) {
                    mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
                    this.b.b();
                } else {
                    // Firework in the hotbar: use it silently. The selected slot is switched only on the server
                    // for a single use and switched straight back, so nothing is ever moved into your hands
                    // and the sword stays in your main hand on screen.
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
                    } else if (inventory.a().isEmpty()) {
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
            LivingEntity target = aura.getTarget();
            if (target == null) {
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

            // Hit alignment: when the hit is (almost) ready and the target is close, look at a real point of
            // its hitbox instead of the "fly through" point. Aiming past/above the target is what made
            // some hits miss on the server side.
            if (this.hitAlign.c().booleanValue() && aura.attackCooldown >= 6
                    && Math.sqrt(AuraUtil.a(eye, target)) <= (double) this.alignRange.c().floatValue()) {
                Vec3d rel = AuraUtil.a(eye, target, (double) this.hitReach.c().floatValue(), true);
                Vec3d point = rel.equals(Vec3d.ZERO) ? enemy : eye.add(rel);
                Rotation hitRotation = Rotation.a(eye, point);
                float hitYaw = AuraUtil.a(mc.player.getYaw(), hitRotation.c(), 1.0f);
                float hitPitch = AuraUtil.a(mc.player.getPitch(), hitRotation.d(), 1.0f);
                Reaxc.getInstance().getModuleProcessor().k().startAiming(new Rotation(hitYaw, hitPitch), this.turnSpeed.c().floatValue(), 1, 2);
                return;
            }

            double lift = Math.max(0.0d, (double) this.liftHeight.c().floatValue() - q());
            Vec3d aim;
            if (this.cutOff.c().booleanValue() && isFleeing(dx, dz, horizontal)) {
                // Get in front of the runner: aim where it will be when we arrive, plus a bit further ahead.
                double mySpeed = Math.max(mc.player.getVelocity().length(), 0.8d);
                double distance = Math.sqrt((dx * dx) + (dz * dz) + ((enemy.y - eye.y) * (enemy.y - eye.y)));
                double ticks = MathHelper.clamp(distance / mySpeed, 1.0d, (double) this.maxPredict.c().floatValue());
                double vy = target.isGliding() ? MathHelper.clamp(this.targetVelocity.y, -1.0d, 1.0d) : 0.0d;
                double hx = this.targetVelocity.x;
                double hz = this.targetVelocity.z;
                double hLen = Math.sqrt((hx * hx) + (hz * hz));
                double lead = (double) this.cutOffLead.c().floatValue();
                double ux = hLen == 0.0d ? 0.0d : hx / hLen;
                double uz = hLen == 0.0d ? 0.0d : hz / hLen;
                aim = new Vec3d(enemy.x + (hx * ticks) + (ux * lead), enemy.y + (vy * ticks) + lift, enemy.z + (hz * ticks) + (uz * lead));
            } else {
                double over = (double) this.overshoot.c().floatValue();
                aim = new Vec3d(enemy.x + (nx * over), enemy.y + lift, enemy.z + (nz * over));
            }
            Rotation aimRotation = Rotation.a(eye, aim);
            float Yaw = AuraUtil.a(mc.player.getYaw(), aimRotation.c(), 1.0f);
            float Pitch = AuraUtil.a(mc.player.getPitch(), aura.attackCooldown <= 3 ? 0.0f : aimRotation.d(), aura.attackCooldown <= 3 ? 1.0f : Math.clamp(aura.attackCooldown / 10.0f, 0.0f, 1.0f));
            Reaxc.getInstance().getModuleProcessor().k().startAiming(new Rotation(Yaw, Pitch), this.turnSpeed.c().floatValue(), 1, 1);
        }
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
        // Moving away from us: velocity points the same way as (target - me).
        double cos = ((hx * dx) + (hz * dz)) / (speed * horizontal);
        return cos > 0.3d;
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
