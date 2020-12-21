package de.labystudio.game.world.block;

import de.labystudio.game.util.EnumBlockFace;

public class BlockGrass extends Block {

    public BlockGrass(int id, int textureSlot) {
        super(id, textureSlot);
    }

    @Override
    public int getTextureForFace(EnumBlockFace face) {
        switch (face) {
            case TOP:
                return this.textureSlotId;
            case BOTTOM:
                return this.textureSlotId + 1;
            default:
                return this.textureSlotId + 2;
        }
    }
}
