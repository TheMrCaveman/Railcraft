/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.plugins.forge;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

/**
 * Created by CovertJaguar on 8/29/2016 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class EntitySearcher {

    public static SearchParameters<EntityMinecart> findMinecarts() {
        return new SearchParameters<EntityMinecart>(EntityMinecart.class);
    }

    public static <T extends Entity> SearchParameters<T> find(Class<T> entityClass) {
        return new SearchParameters<T>(entityClass);
    }

    public static class SearchParameters<T extends Entity> {
        private final Class<T> entityClass;
        private AxisAlignedBB searchBox;
        private Predicate<Entity> filter = Predicates.and(EntitySelectors.NOT_SPECTATING, EntitySelectors.IS_ALIVE);

        public SearchParameters(Class<T> entityClass) {
            this.entityClass = entityClass;
        }

        public List<T> at(World world) {
            return world.getEntitiesWithinAABB(entityClass, searchBox, filter);
        }

        /**
         * @param sensitivity Controls the size of the search box, ranges from
         *                    (-inf, 0.49].
         */
        public SearchParameters<T> inBox(BlockPos pos, float sensitivity) {
            sensitivity = Math.min(sensitivity, 0.49f);
            searchBox = new AxisAlignedBB(pos.getX() + sensitivity, pos.getY() + sensitivity, pos.getZ() + sensitivity,
                    pos.getX() + 1 - sensitivity, pos.getY() + 1 - sensitivity, pos.getZ() + 1 - sensitivity);
            return this;
        }

        /**
         * @param sensitivity Controls the size of the search box, ranges from
         *                    (-inf, 0.49].
         */
        public SearchParameters<T> inFloorBox(BlockPos pos, float sensitivity) {
            sensitivity = Math.min(sensitivity, 0.49f);
            searchBox = new AxisAlignedBB(pos.getX() + sensitivity, pos.getY(), pos.getZ() + sensitivity,
                    pos.getX() + 1 - sensitivity, pos.getY() + 1 - sensitivity, pos.getZ() + 1 - sensitivity);
            return this;
        }

        public SearchParameters<T> with(Predicate<Entity> filter) {
            this.filter = filter;
            return this;
        }

        @SafeVarargs
        public final SearchParameters<T> andWith(Predicate<Entity>... filters) {
            if (!ArrayUtils.isEmpty(filters))
                this.filter = Predicates.and(filter, Predicates.and(filters));
            return this;
        }
    }
}
