package com.frolo.muse.ui.main.library.albums.album

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.frolo.muse.R
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.asNonZeroDurationInMs
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.SongAdapter
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_song_of_album.view.*


class AlbumSongAdapter(requestManager: RequestManager): SongAdapter(requestManager) {
    override fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song_of_album, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int, item: Song, selected: Boolean, selectionChanged: Boolean) {
        with(holder.itemView) {
            val res = resources
            tv_song_name.text = item.getNameString(res)
            tv_artist_name.text = item.getArtistString(res)
            tv_duration.text = item.duration.asNonZeroDurationInMs()

            if (position != playingPosition) {
                mini_visualizer.visibility = View.GONE
                mini_visualizer.setAnimating(false)
            } else {
                mini_visualizer.visibility = View.VISIBLE
                mini_visualizer.setAnimating(isPlaying)
            }
            //changeBackground(holder.itemView, holder.cNormal, holder.cSelected, selected, false);

            imv_check.setChecked(selected, selectionChanged)
        }
    }
}