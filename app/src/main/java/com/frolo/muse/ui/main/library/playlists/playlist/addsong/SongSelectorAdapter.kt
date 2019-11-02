package com.frolo.muse.ui.main.library.playlists.playlist.addsong

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.RequestManager
import com.frolo.muse.R
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.getAlbumString
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.SongAdapter
import kotlinx.android.synthetic.main.item_select_song.view.*


class SongSelectorAdapter constructor(
        requestManager: RequestManager
): SongAdapter(requestManager) {

    override fun onCreateBaseViewHolder(
            parent: ViewGroup,
            viewType: Int): SongViewHolder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_select_song, parent, false)

        return SongSelectorViewHolder(view)
    }

    override fun onBindViewHolder(
            holder: SongViewHolder,
            position: Int,
            item: Song,
            selected: Boolean, selectionChanged: Boolean) {

        val selectorViewHolder = holder as SongSelectorViewHolder
        with(selectorViewHolder.itemView) {
            val res = resources
            tv_song_name.text = item.getNameString(res)
            tv_album_name.text = item.getAlbumString(res)
            tv_duration.text = item.getDurationString()

            if (position != playingPosition) {
                mini_visualizer.visibility = View.GONE
                mini_visualizer.setAnimating(false)
            } else {
                mini_visualizer.visibility = View.VISIBLE
                mini_visualizer.setAnimating(isPlaying)
            }

            chb_select_song.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_note_rounded_placeholder)!!)

            chb_select_song.setChecked(checked = selected, animate = selectionChanged)

            isSelected = selected
        }
    }

    class SongSelectorViewHolder(itemView: View): SongViewHolder(itemView) {
        override val viewOptionsMenu: View? = null
    }

}