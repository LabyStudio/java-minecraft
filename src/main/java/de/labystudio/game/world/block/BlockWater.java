package de.labystudio.game.world.block;

import de.labystudio.game.render.world.IWorldAccess;
import de.labystudio.game.util.EnumBlockFace;

public class BlockWater extends Block {

    public BlockWater(int id, int textureSlot) {
        super(id, textureSlot);

        this.boundingBox.maxY = 1.0F - 0.12F;
    }

    @Override
    public float getOpacity() {
        return 0.3F;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean shouldRenderFace(IWorldAccess world, int x, int y, int z, EnumBlockFace face) {
        short typeId = world.getBlockAt(x + face.x, y + face.y, z + face.z);
        return typeId == 0 || typeId != this.id && Block.getById(typeId).isTransparent();
    }
}
