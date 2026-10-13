package aethereal.module.movement;

import aethereal.core.*;
import aethereal.core.Module;
import aethereal.event.TickEvent;
import aethereal.module.combat.Aura;
import aethereal.handler.InventoryHandler;
import aethereal.module.combat.AuraUtil;
import aethereal.util.CounterUtil;
import aethereal.util.InventoryUtil;
import aethereal.util.Rotation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

@ModuleRegister(name = "Elytra Target", description = "Aims at an enemy while flying with elytra and boosts with a firework from the hotbar without moving it into your hand", category = Category.Movement)
public class ElytraTarget extends Module {
    private final CounterUtil b = new CounterUtil();
    private boolean swapped;
    private int slot = -1;
    private int wait;

    @EventTarget
    public void a(TickEvent event) {
        Aura aura = Reaxc.getInstance().getModuleProcessor().t().B();
        InventoryHandler inventory = Reaxc.getInstance().getModuleProcessor().v().getInventoryHandler();
        if (aura.m() && mc.player.isGliding()) {
            // The sword stays in the main hand; the firework is never held visibly.
            boolean totemBusy = Reaxc.getInstance().getModuleProcessor().t().V().b;
            boolean shouldFire = (this.b.a(150L) && mc.player.getVelocity().length() < 1.5d) || aura.attackCooldown == 1;
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
                return;
            }
            Vec3d eye = mc.player.getEyePos();
            Vec3d enemy = target.getBoundingBox().getCenter();
            double dx = enemy.x - eye.x;
            double dz = enemy.z - eye.z;
            double horizontal = Math.sqrt((dx * dx) + (dz * dz));
            double nx = horizontal == 0.0d ? 0.0d : dx / horizontal;
            double nz = horizontal == 0.0d ? 0.0d : dz / horizontal;
            double lift = Math.max(0.0d, 3.0d - q());
            Vec3d aim = new Vec3d(enemy.x + (nx * 4.0d), enemy.y + lift, enemy.z + (nz * 4.0d));
            Rotation aimRotation = Rotation.a(eye, aim);
            float Yaw = AuraUtil.a(mc.player.getYaw(), aimRotation.c(), 1.0f);
            float Pitch = AuraUtil.a(mc.player.getPitch(), aura.attackCooldown <= 3 ? 0.0f : aimRotation.d(), aura.attackCooldown <= 3 ? 1.0f : Math.clamp(aura.attackCooldown / 10.0f, 0.0f, 1.0f));
            Reaxc.getInstance().getModuleProcessor().k().startAiming(new Rotation(Yaw, Pitch), 180.0f, 1, 1);
        }
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
