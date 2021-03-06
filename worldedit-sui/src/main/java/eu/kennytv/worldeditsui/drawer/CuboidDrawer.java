/*
 * WorldEditSUI - https://git.io/wesui
 * Copyright (C) 2018 KennyTV (https://github.com/KennyTV)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.kennytv.worldeditsui.drawer;

import com.sk89q.worldedit.regions.Region;
import eu.kennytv.worldeditsui.WorldEditSUIPlugin;
import eu.kennytv.worldeditsui.compat.SimpleVector;
import eu.kennytv.worldeditsui.drawer.base.DrawerBase;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class CuboidDrawer extends DrawerBase {

    //TODO: probably needs a complete rework
    CuboidDrawer(final WorldEditSUIPlugin plugin) {
        super(plugin);
    }

    @Override
    public void draw(final Player player, final Region region) {
        this.draw(player, region, false);
    }

    @Override
    public void draw(final Player player, final Region region, final boolean copySelection) {
        final double width = region.getWidth();
        final double length = region.getLength();
        final double height = region.getHeight();
        final SimpleVector minimumVector = plugin.getRegionHelper().getMinimumPoint(region);
        final Location minimumPoint = new Location(player.getWorld(), minimumVector.getX(), minimumVector.getY(), minimumVector.getZ());

        final double maxTicksX = width * settings.getParticlesPerBlock() - 1;
        final double maxTicksZ = length * settings.getParticlesPerBlock() - 1;
        double maxGridTicks = 0;
        double maxTopGridTicksX = 0;
        double maxTopGridTicksZ = 0;
        int gridSpaceX = 0;
        int gridSpaceZ = 0;
        int topGridSpace = 0;
        if (settings.hasAdvancedGrid(copySelection)) {
            gridSpaceX = settings.getParticlesPerBlock() * ((int) ((width * height) / AREA_FACTOR) + 1);
            gridSpaceZ = settings.getParticlesPerBlock() * ((int) ((length * height) / AREA_FACTOR) + 1);
            topGridSpace = settings.getParticlesPerBlock() * ((int) ((width * length) / AREA_FACTOR) + 1);
            maxGridTicks = height * settings.getParticlesPerGridBlock(copySelection) - 1;
            maxTopGridTicksX = length * settings.getParticlesPerGridBlock(copySelection) - 1;
            maxTopGridTicksZ = width * settings.getParticlesPerGridBlock(copySelection) - 1;
        }

        drawLines(player, minimumPoint.clone(), gridSpaceX, topGridSpace, maxTicksX, maxGridTicks, maxTopGridTicksX, height, true, copySelection);
        drawLines(player, minimumPoint.clone().add(0, 0, length), gridSpaceX, 0, maxTicksX, maxGridTicks, 0, height, true, copySelection);
        drawLines(player, minimumPoint.clone(), gridSpaceZ, topGridSpace, maxTicksZ, maxGridTicks, maxTopGridTicksZ, height, false, copySelection);
        drawLines(player, minimumPoint.clone().add(width, 0, 0), gridSpaceZ, 0, maxTicksZ, maxGridTicks, 0, height, false, copySelection);
        drawPillarsAndGrid(player, minimumPoint, gridSpaceX, gridSpaceZ, height, width, length, copySelection);
    }

    private void drawLines(final Player player, final Location location, final int gridSpace, final int topGridSpace,
                           final double maxTicks, final double maxGridTicks, final double maxTopGridTicks,
                           final double height, final boolean x, final boolean copySelection) {
        // Lower row (with vertical grid)
        final double oldX = location.getX();
        final double oldZ = location.getZ();
        int blocks = 0;
        for (int i = 0; i < maxTicks; i++) {
            if (settings.hasAdvancedGrid(copySelection)) {
                if (blocks % gridSpace == 0 && i != 0) {
                    final Location clone = location.clone();
                    for (double j = 0; j < maxGridTicks; j++) {
                        clone.add(0, settings.getParticleGridSpace(copySelection), 0);
                        playEffect(clone, player, copySelection);
                    }
                }
                if (topGridSpace != 0 && blocks % topGridSpace == 0 && i != 0) {
                    tickGrid(player, location, maxTopGridTicks, x, copySelection);
                }
                blocks++;
            }

            if (x)
                location.add(settings.getParticleSpace(), 0, 0);
            else
                location.add(0, 0, settings.getParticleSpace());
            playEffect(location, player, copySelection);
        }

        // Upper row
        location.setX(oldX);
        location.setZ(oldZ);
        location.setY(location.getY() + height);
        blocks = 0;
        for (double i = 0; i < maxTicks; i++) {
            if (settings.hasAdvancedGrid(copySelection) && topGridSpace != 0 && blocks++ % topGridSpace == 0 && i != 0) {
                tickGrid(player, location, maxTopGridTicks, x, copySelection);
            }

            if (x)
                location.add(settings.getParticleSpace(), 0, 0);
            else
                location.add(0, 0, settings.getParticleSpace());
            playEffect(location, player, copySelection);
        }
    }

    private void tickGrid(final Player player, final Location location, final double maxTopGridTicks, final boolean x, final boolean copySelection) {
        final Location clone = location.clone();
        for (double j = 0; j < maxTopGridTicks; j++) {
            if (x)
                clone.add(0, 0, settings.getParticleGridSpace(copySelection));
            else
                clone.add(settings.getParticleGridSpace(copySelection), 0, 0);
            playEffect(clone, player, copySelection);
        }
    }

    private void drawPillarsAndGrid(final Player player, final Location minimum, final int gridSpaceX, final int gridSpaceZ,
                                    final double height, final double width, final double length, final boolean copySelection) {
        final boolean advancedGridEnabled = settings.hasAdvancedGrid(copySelection);

        final double x = minimum.getX();
        final double y = minimum.getY();
        final double z = minimum.getZ();
        final double gridSpace = advancedGridEnabled ? settings.getParticleGridSpace(copySelection) : 0;
        final double maxTicks = height * settings.getParticlesPerBlock();
        final double maxGridTicksX = advancedGridEnabled ? width * settings.getParticlesPerGridBlock(copySelection) - 1 : 0;
        final double maxGridTicksZ = advancedGridEnabled ? length * settings.getParticlesPerGridBlock(copySelection) - 1 : 0;
        setGrid(player, minimum, gridSpaceX, maxTicks, maxGridTicksX, gridSpace, 0, copySelection);
        minimum.setX(x + width);
        minimum.setY(y);
        minimum.setZ(z + length);
        setGrid(player, minimum, gridSpaceX, maxTicks, maxGridTicksX, -gridSpace, 0, copySelection);
        minimum.setX(x + width);
        minimum.setY(y);
        minimum.setZ(z);
        setGrid(player, minimum, gridSpaceZ, maxTicks, maxGridTicksZ, 0, gridSpace, copySelection);
        minimum.setX(x);
        minimum.setY(y);
        minimum.setZ(z + length);
        setGrid(player, minimum, gridSpaceZ, maxTicks, maxGridTicksZ, 0, -gridSpace, copySelection);
    }

    private void setGrid(final Player player, final Location location, final int gridSpace, final double maxTicks, final double maxGridTicks, final double xAddition, final double zAddition, final boolean copySelection) {
        int blocks = 0;
        for (int i = 0; i < maxTicks; i++) {
            // Horizontal grid
            if (settings.hasAdvancedGrid(copySelection) && blocks++ % gridSpace == 0 && i != 0) {
                final Location clone = location.clone();
                for (double j = 0; j < maxGridTicks; j++) {
                    clone.add(xAddition, 0, zAddition);
                    playEffect(clone, player, copySelection);
                }
            }

            // Pillar
            location.add(0, settings.getParticleSpace(), 0);
            playEffect(location, player, copySelection);
        }
    }
}
