package com.vfpbedrock.patcher.mixins.local.block;

import com.vfpbedrock.patcher.interfaces.IBedrockBlockState;
import com.vfpbedrock.patcher.other.RandomMethods;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.raphimc.viabedrock.api.BedrockProtocolVersion;
import net.raphimc.viabedrock.api.model.BedrockBlockState;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class BlockMixins {
    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    public void patchCustomBlockCollision(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if ((Object)this != Blocks.COARSE_DIRT) { // 100% not a custom block.
            return;
        }

        if (!RandomMethods.hasVFPInit()) {
            return;
        }

        final UserConnection user = ViaFabricPlus.getImpl().getPlayNetworkUserConnection();
        if (user == null || ViaFabricPlus.getImpl().getTargetVersion() != BedrockProtocolVersion.bedrockLatest) {
            return;
        }

        final int bedrockId = user.get(ChunkTracker.class).getBlockState(0, new BlockPosition(pos.getX(), pos.getY(), pos.getZ()));
        final net.raphimc.viabedrock.api.model.BlockState bedrockState = user.get(BlockStateRewriter.class).blockState(bedrockId);
        if (!(bedrockState instanceof BedrockBlockState)) {
            return;
        }

        VoxelShape shape = ((IBedrockBlockState)bedrockState).vfpbedrockpatcher$collisionBox();
        if (shape != null) {
            cir.setReturnValue(shape);
        }
    }

    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    public void patchCustomBlockOutline(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if ((Object)this != Blocks.COARSE_DIRT) { // 100% not a custom block.
            return;
        }

        if (!RandomMethods.hasVFPInit()) {
            return;
        }

        final UserConnection user = ViaFabricPlus.getImpl().getPlayNetworkUserConnection();
        if (user == null || ViaFabricPlus.getImpl().getTargetVersion() != BedrockProtocolVersion.bedrockLatest) {
            return;
        }

        final int bedrockId = user.get(ChunkTracker.class).getBlockState(0, new BlockPosition(pos.getX(), pos.getY(), pos.getZ()));
        final net.raphimc.viabedrock.api.model.BlockState bedrockState = user.get(BlockStateRewriter.class).blockState(bedrockId);
        if (!(bedrockState instanceof BedrockBlockState)) {
            return;
        }

        VoxelShape shape = ((IBedrockBlockState)bedrockState).vfpbedrockpatcher$selectionBox();
        if (shape != null) {
            cir.setReturnValue(shape);
        }
    }
}
