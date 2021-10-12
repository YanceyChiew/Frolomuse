package com.frolo.muse.model.media;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public final class SongFilter {

    public static final long DURATION_NOT_SET = -1L;
    public static final long TIME_NOT_SET = -1L;
    public static final long ID_NOT_SET = Media.NO_ID;
    public static final String NAME_PIECE_NOT_SET = null;
    public static final String FOLDER_PATH_NOT_SET = null;
    public static final String FILEPATH_NOT_SET = null;

    private static final Set<SongType> ALL_SONG_TYPES;

    static {
        SongType[] songTypesValues= SongType.values();
        ALL_SONG_TYPES = new HashSet<>(songTypesValues.length);
        ALL_SONG_TYPES.addAll(Arrays.asList(songTypesValues));
    }

    private static final SongFilter NONE = new SongFilter(ALL_SONG_TYPES,
            NAME_PIECE_NOT_SET, FOLDER_PATH_NOT_SET, FILEPATH_NOT_SET,
            ID_NOT_SET, ID_NOT_SET, ID_NOT_SET, DURATION_NOT_SET, DURATION_NOT_SET, TIME_NOT_SET);

    @NotNull
    public static SongFilter none() {
        return NONE;
    }

    @NotNull
    public static SongFilter ofNamePiece(@Nullable String titleFilter) {
        return new SongFilter(ALL_SONG_TYPES, titleFilter, FOLDER_PATH_NOT_SET, FILEPATH_NOT_SET,
                ID_NOT_SET, ID_NOT_SET, ID_NOT_SET, DURATION_NOT_SET, DURATION_NOT_SET, TIME_NOT_SET);
    }

    // Properties
    @NotNull
    private final Set<SongType> types;

    private final long albumId;
    private final long artistId;
    private final long genreId;

    @Nullable
    private final String namePiece;
    @Nullable
    private final String folderPath;
    @Nullable
    private final String filepath;

    private final long minDuration;
    private final long maxDuration;

    private final long timeAdded;

    SongFilter(
            @NotNull Set<SongType> types, @Nullable String namePiece,
            @Nullable String folderPath, @Nullable String filepath,
            long albumId, long artistId, long genreId,
            long minDuration, long maxDuration,
            long timeAdded) {
        this.types = types;
        this.namePiece = namePiece;
        this.folderPath = folderPath;
        this.filepath = filepath;
        this.albumId = albumId;
        this.artistId = artistId;
        this.genreId = genreId;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.timeAdded = timeAdded;
    }

    @NotNull
    public Set<SongType> getTypes() {
        return types;
    }

    @Nullable
    public String getNamePiece() {
        return namePiece;
    }

    @Nullable
    public String getFolderPath() {
        return folderPath;
    }

    @Nullable
    public String getFilepath() {
        return filepath;
    }

    public long getAlbumId() {
        return albumId;
    }

    public long getArtistId() {
        return artistId;
    }

    public long getGenreId() {
        return genreId;
    }

    public long getMinDuration() {
        return minDuration;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public long getTimeAdded() {
        return timeAdded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongFilter that = (SongFilter) o;
        return albumId == that.albumId &&
                artistId == that.artistId &&
                genreId == that.genreId &&
                minDuration == that.minDuration &&
                maxDuration == that.maxDuration &&
                timeAdded == that.timeAdded &&
                types.containsAll(that.types) && that.types.containsAll(types) &&
                Objects.equals(namePiece, that.namePiece) &&
                Objects.equals(folderPath, that.folderPath) &&
                Objects.equals(filepath, that.filepath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(types, albumId, artistId, genreId, namePiece,
                folderPath, filepath, minDuration, maxDuration, timeAdded);
    }

    @NotNull
    public Builder newBuilder() {
        Builder builder = new Builder();
        for (SongType type : types) {
            builder.addType(type);
        }
        builder.setNamePiece(namePiece);
        builder.setFolderPath(folderPath);
        builder.setFilepath(filepath);
        builder.setAlbumId(albumId);
        builder.setArtistId(artistId);
        builder.setGenreId(genreId);
        builder.setMinDuration(minDuration);
        builder.setMaxDuration(maxDuration);
        builder.setTimeAdded(timeAdded);
        return builder;
    }

    public static final class Builder {
        private final Set<SongType> types = new HashSet<>(6);
        private String namePiece = NAME_PIECE_NOT_SET;
        private String folderPath = FOLDER_PATH_NOT_SET;
        private String filepath = FILEPATH_NOT_SET;
        private long albumId = ID_NOT_SET;
        private long artistId = ID_NOT_SET;
        private long genreId = ID_NOT_SET;
        private long minDuration = DURATION_NOT_SET;
        private long maxDuration = DURATION_NOT_SET;
        private long timeAdded = TIME_NOT_SET;

        public Builder addType(@NotNull SongType type) {
            types.add(type);
            return this;
        }

        public Builder setNamePiece(@Nullable String piece) {
            namePiece = piece;
            return this;
        }

        public Builder setFolderPath(@Nullable String path) {
            folderPath = path;
            return this;
        }

        public Builder setFilepath(@Nullable String path) {
            filepath = path;
            return this;
        }

        public Builder setAlbumId(long id) {
            albumId = id;
            return this;
        }

        public Builder setArtistId(long id) {
            artistId = id;
            return this;
        }

        public Builder setGenreId(long id) {
            genreId = id;
            return this;
        }

        public Builder setMinDuration(long duration) {
            minDuration = duration;
            return this;
        }

        public Builder setMaxDuration(long duration) {
            maxDuration = duration;
            return this;
        }

        public Builder setTimeAdded(long time) {
            timeAdded = time;
            return this;
        }

        @NotNull
        public SongFilter build() {
            return new SongFilter(types, namePiece, folderPath, filepath,
                    albumId, artistId, genreId, minDuration, maxDuration, timeAdded);
        }
    }
}
