package net.william278.husktowns.hook;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.claim.World;
import net.william278.husktowns.town.Town;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class BlueMapHook extends MapHook {

    private Map<UUID, MarkerSet> markerSets;

    public BlueMapHook(@NotNull HuskTowns plugin) {
        super(plugin, "BlueMap");
    }

    @Override
    public void onEnable() {
        BlueMapAPI.onEnable(api -> {
            clearAllMarkers();

            this.markerSets = new HashMap<>();
            for (World world : plugin.getWorlds()) {
                getMapWorld(world).ifPresent(mapWorld -> {
                    final MarkerSet markerSet = MarkerSet.builder()
                            .label(plugin.getSettings().webMapMarkerSetName)
                            .build();
                    for (BlueMapMap map : mapWorld.getMaps()) {
                        map.getMarkerSets().put(plugin.getKey(map.getId()).toString(), markerSet);
                    }
                    markerSets.put(world.getUuid(), markerSet);
                });
            }

            for (Town town : plugin.getTowns()) {
                setClaimMarkers(town);
            }
        });
    }

    @NotNull
    private ShapeMarker getClaimMarker(@NotNull TownClaim claim) {
        final int x = claim.claim().getChunk().getX() * 16;
        final int z = claim.claim().getChunk().getZ() * 16;
        return ShapeMarker.builder()
                .label(claim.town().getName())
                .fillColor(new Color(claim.town().getColor().getRed(),
                        claim.town().getColor().getGreen(),
                        claim.town().getColor().getBlue(), 0.5f))
                .shape(Shape.createRect(x, z, x + 16, z + 16), 64)
                .lineWidth(0)
                .depthTestEnabled(true)
                .build();
    }

    @NotNull
    private String getClaimMarkerKey(@NotNull TownClaim claim) {
        return plugin.getKey(
                claim.town().getName().toLowerCase(),
                Integer.toString(claim.claim().getChunk().getX()),
                Integer.toString(claim.claim().getChunk().getZ())
        ).toString();
    }

    @Override
    public void setClaimMarker(@NotNull TownClaim claim, @NotNull World world) {
        getMarkerSet(world).ifPresent(markerSet -> markerSet.put(getClaimMarkerKey(claim), getClaimMarker(claim)));
    }

    @Override
    public void removeClaimMarker(@NotNull TownClaim claim, @NotNull World world) {
        getMarkerSet(world).ifPresent(markerSet -> markerSet.remove(getClaimMarkerKey(claim)));
    }

    @Override
    public void setClaimMarkers(@NotNull Town town) {
        for (World world : plugin.getWorlds()) {
            getMarkerSet(world).ifPresent(markerSet -> plugin.getClaimWorld(world).ifPresent(claimWorld -> {
                for (Claim claim : claimWorld.getClaims().getOrDefault(town.getId(), List.of())) {
                    final TownClaim townClaim = new TownClaim(town, claim);
                    markerSet.put(getClaimMarkerKey(townClaim), getClaimMarker(townClaim));
                }
            }));
        }
    }

    @Override
    public void removeClaimMarkers(@NotNull Town town) {
        for (World world : plugin.getWorlds()) {
            getMarkerSet(world).ifPresent(markerSet -> plugin.getClaimWorld(world).ifPresent(claimWorld -> {
                for (Claim claim : claimWorld.getClaims().getOrDefault(town.getId(), List.of())) {
                    markerSet.remove(getClaimMarkerKey(new TownClaim(town, claim)));
                }
            }));
        }
    }

    @Override
    public void clearAllMarkers() {
        if (markerSets != null) {
            for (MarkerSet markerSet : markerSets.values()) {
                for (String markerId : markerSet.getMarkers().keySet()) {
                    markerSet.remove(markerId);
                }
            }
        }
    }

    @NotNull
    private Optional<MarkerSet> getMarkerSet(@NotNull World world) {
        return markerSets == null ? Optional.empty() : Optional.ofNullable(markerSets.get(world.getUuid()));
    }

    @NotNull
    private Optional<BlueMapWorld> getMapWorld(@NotNull World world) {
        return BlueMapAPI.getInstance().flatMap(api -> api.getWorld(world.getUuid()));
    }

}