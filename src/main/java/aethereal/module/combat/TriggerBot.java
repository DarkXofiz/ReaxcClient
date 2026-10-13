package aethereal.module.combat;

import aethereal.core.*;
import aethereal.core.Module;
import aethereal.event.InputEvent;
import aethereal.event.TickEvent;
import aethereal.event.WillLandEvent;
import aethereal.setting.BooleanSetting;
import aethereal.setting.ModeSetting;
import aethereal.setting.MultiModeSetting;
import aethereal.ui.screen.AssistantScreen;
import aethereal.ui.screen.GUIScreen;
import aethereal.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@ModuleRegister(name = "Trigger Bot", description = "Automatically hits when the crosshair is on a target", category = Category.Combat)
public class TriggerBot extends Module {
    final BooleanSetting b = new BooleanSetting("Target chasing", false);
    private final MultiModeSetting f = new MultiModeSetting("Attack targets", new BooleanSetting("Players", true), new BooleanSetting("Animals", false), new BooleanSetting("Mobs", false), new BooleanSetting("Friends", true));
    private final MultiModeSetting g = new MultiModeSetting("Extra", new BooleanSetting("Critical hits only", true), new BooleanSetting("Adaptive hits", true), new BooleanSetting("Random misses", true));
    private final MultiModeSetting h = new MultiModeSetting("Don't hit when", new BooleanSetting("Using an item", true), new BooleanSetting("Container open", true), new BooleanSetting("Enemy behind a wall", false));
    private final ModeSetting i = new ModeSetting("Sprint reset", "Legit", "Legit", "Rage");
    private final ModeSetting j = new ModeSetting("Target selection", "Free", "Free", "Locking");
    private final CounterUtil k = new CounterUtil();
    public int d;
    boolean c;
    boolean e = false;
    private int stallTicks;
    private int m;
    private float targetYaw;
    private LivingEntity target;

    public TriggerBot() {
        a(this.f, this.g, this.h, this.i, this.j, this.b);
    }

    public int getAttackCount() {
        return this.d;
    }

    public LivingEntity getTarget() {
        return this.target;
    }

    @Override
    public void c() {
        super.c();
        this.target = null;
        this.stallTicks = 0;
        this.d = 0;
        this.k.b();
    }

    @EventTarget
    public void onInput(InputEvent e) {
        if (this.b.c().booleanValue() && this.target != null) {
            MoveUtil.a(e, this.targetYaw, 3);
        }
        if (this.target != null) {
            Vec3d targetPosition = AuraUtil.a(mc.player.getEyePos(), this.target, 3.0d, true);
            this.targetYaw = targetPosition == Vec3d.ZERO ? Look.b() : (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(targetPosition.z, targetPosition.x)) - 90.0d);
        }
        if (this.i.l("Legit") && this.stallTicks > 0) {
            e.setForward(0.0f);
            e.setStrafe(0.0f);
            this.stallTicks--;
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        t();
    }

    @EventTarget
    public void onWillLand(WillLandEvent e) {
        this.c = e.b() && !mc.player.isOnGround();
    }

    private void t() {
        this.d++;
        v();
        if (this.target != null && this.g.a("Random misses").c().booleanValue() && this.d >= 2 && this.m >= 30 && (((Math.random() > 0.5d && this.d >= 1) || this.d == 4) && (!this.e || !AuraUtil.a(mc.player.getYaw(), mc.player.getPitch(), 3.0d, this.target, false)))) {
            ((platform.inject.invokers.MinecraftClientInvoker) mc).invokeDoAttack();
            this.e = !this.e;
            this.m = (int) MathUtil.a(-10.0f, 10.0f);
        }
        if (this.target != null && AuraUtil.a(this.d, this.target, false)) {
            this.stallTicks = 1;
        }
        u();
    }

    private void u() {
        if (this.target == null || !q()) {
            return;
        }
        if (!AuraUtil.a(mc.player.getYaw(), mc.player.getPitch(), 3.0d, this.target, !this.h.a("Enemy behind a wall").c().booleanValue())) {
            return;
        }
        boolean skip = false;
        if ((Reaxc.getInstance().getModuleProcessor().t().H().e || (mc.player.fallDistance > 2.0f && Reaxc.getInstance().getModuleProcessor().t().H().c.c().booleanValue())) && InventoryUtil.b(Items.MACE) != -1) {
            if (mc.player.fallDistance < 1.5f) {
                return;
            }
            double landDist = MaceUtil.a(mc.player, mc.world).map(pos -> {
                return Double.valueOf(pos.distanceTo(this.target.getPos()));
            }).orElse(Double.valueOf(33.0d)).doubleValue();
            boolean hitNow = landDist > 2.0d;
            if ((!this.c && !MaceUtil.b() && Reaxc.getInstance().getModuleProcessor().t().H().b.c().booleanValue() && !hitNow) || !MaceUtil.a() || mc.player.isGliding()) {
                return;
            } else {
                skip = true;
            }
        }
        if (skip || !w()) {
            mc.interactionManager.attackEntity(mc.player, this.target);
            mc.player.swingHand(Hand.MAIN_HAND);
            if (Math.random() <= 0.899999737739563d) {
                this.m++;
            }
            this.d = 0;
        }
    }

    private void v() {
        if (this.j.l("Locking")) {
            if (!a(this.target) || (MaceUtil.a() && !this.c && !mc.player.getItemCooldownManager().isCoolingDown(Items.MACE.getDefaultStack()))) {
                this.target = d(false);
                return;
            }
            return;
        }
        LivingEntity aimed = d(true);
        if (aimed != null) {
            this.target = aimed;
            this.k.b();
        } else if (this.target != null && this.k.a(1000L)) {
            this.target = null;
        }
    }

    private boolean a(LivingEntity entity) {
        return entity != null && entity.isAlive() && !entity.isSpectator() && entity != mc.player && b(entity) && d(entity);
    }

    private boolean b(LivingEntity entity) {
        if (Reaxc.getInstance().getModuleProcessor().t().G().m() && mc.player.isGliding()) {
            return true;
        }
        return AuraUtil.a((Entity) entity, 4.0d + (mc.player.getVelocity().length() * 3.0d) + ((double) ((InventoryUtil.b(Items.MACE) == -1 || ((double) mc.player.fallDistance) <= 1.5d) ? 0.0f : 1.5f)) + ((double) ((Reaxc.getInstance().getModuleProcessor().t().H().m() && InventoryUtil.b(Items.MACE) != -1 && MaceUtil.a(mc.player, mc.world).map(p -> {
            return Boolean.valueOf(mc.player.getY() - p.getY() > 2.0d);
        }).orElse(false).booleanValue()) ? 10 : 0)));
    }

    private boolean c(LivingEntity entity) {
        return Stream.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET).anyMatch(s -> {
            return entity.getEquippedStack(s).getItem() instanceof ArmorItem;
        });
    }

    private boolean w() {
        if (!((platform.inject.accessors.ClientPlayerEntityAccessor) mc.player).getWasSprinting() || mc.player.isTouchingWater() || mc.player.isInLava() || mc.player.isSwimming() || mc.player.isOnGround()) {
            return false;
        }
        if (this.i.l("Rage")) {
            ((platform.inject.accessors.ClientPlayerEntityAccessor) mc.player).setWasSprinting(false);
            mc.player.setSprinting(false);
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            this.stallTicks = 1;
            return false;
        }
        this.stallTicks = 1;
        return ((platform.inject.accessors.ClientPlayerEntityAccessor) mc.player).getWasSprinting();
    }

    public boolean q() {
        if (this.h.a("Using an item") != null && this.h.a("Using an item").c().booleanValue() && mc.player.isUsingItem() && mc.player.getItemUseTimeLeft() > 0 && this.d >= 8) {
            this.d = 8;
            return false;
        }
        if ((this.h.a("Container open") != null && this.h.a("Container open").c().booleanValue() && mc.currentScreen != null && !(mc.currentScreen instanceof GUIScreen) && !(mc.currentScreen instanceof AssistantScreen)) || !AuraUtil.a(this.target, 3.0d)) {
            return false;
        }
        if (Reaxc.getInstance().getModuleProcessor().t().H().e) {
            if (mc.player.getItemCooldownManager().isCoolingDown(mc.player.getMainHandStack())) {
                return false;
            }
        } else if (mc.player.fallDistance > 1.5f) {
            if (mc.player.getItemCooldownManager().isCoolingDown(mc.player.getMainHandStack()) || this.d <= 3) {
                return false;
            }
        } else if (MaceUtil.a()) {
            if (mc.player.getItemCooldownManager().isCoolingDown(mc.player.getMainHandStack()) || mc.player.getAttackCooldownProgress(0.5f) < 0.9f) {
                return false;
            }
        } else if (mc.player.getAttackCooldownProgress(0.5f) < 0.9f || this.d < 10) {
            return false;
        }
        return AuraUtil.c() || (this.g.a("Adaptive hits").c().booleanValue() && mc.player.isOnGround() && !mc.player.input.playerInput.jump()) || !AuraUtil.b();
    }

    private LivingEntity d(boolean aimed) {
        if (!aimed) {
            return (this.h.a("Enemy behind a wall").c().booleanValue() ? e(false).or(() -> {
                return e(true);
            }) : e(true)).orElse(null);
        }
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        return x().filter(e -> {
            return AuraUtil.a(yaw, pitch, 3.0d, e, !this.h.a("Enemy behind a wall").c().booleanValue());
        }).min(Comparator.comparingDouble((v0) -> {
            return AuraUtil.a(v0);
        })).orElse(null);
    }

    private Optional<LivingEntity> e(boolean allowBehindWalls) {
        Comparator<? super LivingEntity> comparatorComparingDouble;
        Vec3d eye = mc.player.getEyePos();
        if (MaceUtil.a()) {
            Vec3d landing = MaceUtil.a(mc.player, mc.world).orElse(null);
            Vec3d landingEye = landing != null ? landing.add(0.0d, mc.player.getStandingEyeHeight(), 0.0d) : null;
            comparatorComparingDouble = Comparator.comparing((LivingEntity e) ->
                    Boolean.valueOf(!AuraUtil.a(eye, e, 4.0d) && (landingEye == null || !AuraUtil.a(landingEye, e, 4.0d)))
            ).thenComparing((LivingEntity e2) ->
                    Boolean.valueOf(mc.player.fallDistance > 1.0f && !c(e2))
            ).thenComparingDouble((LivingEntity v0) -> AuraUtil.a(v0));
        } else {
            comparatorComparingDouble = Comparator.comparingDouble(e3 -> {
                return Math.acos(MathHelper.clamp(Vec3d.fromPolar(mc.player.getPitch(), mc.player.getYaw()).dotProduct(e3.getBoundingBox().getCenter().subtract(eye).normalize()), -1.0d, 1.0d));
            });
        }
        Stream<LivingEntity> stream = x();
        if (!allowBehindWalls) {
            stream = stream.filter(e4 -> {
                return AuraUtil.a(eye, e4, 4.0d);
            });
        }
        return stream.min(comparatorComparingDouble);
    }

    private Stream<LivingEntity> x() {
        return StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .filter(this::a);
    }

    private boolean d(LivingEntity e) {
        if (e instanceof PlayerEntity p) {
            return this.f.a("Players").c().booleanValue() && (this.f.a("Friends").c().booleanValue() || !Reaxc.getInstance().getModuleProcessor().e().d(p.getName().getString()));
        }
        if (e instanceof MobEntity) {
            return this.f.a("Mobs").c().booleanValue();
        }
        if (e instanceof AnimalEntity) {
            return this.f.a("Animals").c().booleanValue();
        }
        return false;
    }
}
