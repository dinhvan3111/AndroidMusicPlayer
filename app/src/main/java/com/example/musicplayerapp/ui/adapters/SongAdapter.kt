package com.example.musicplayerapp.ui.adapters

import android.annotation.SuppressLint
import android.content.ContentUris
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayerapp.R
import com.example.musicplayerapp.databinding.ItemSongBinding
import com.example.musicplayerapp.models.Song
import java.util.Collections

class SongAdapter(
    private val dragListener: IOnStartDragListener
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    inner class SongViewHolder(val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root)

    private var onItemClickListener: ((Song) -> Unit)? = null
    private val differCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(
            oldItem: Song,
            newItem: Song
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Song,
            newItem: Song
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SongViewHolder {
        val binding = ItemSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun moveItem(from: Int, to: Int) {
        // make a copy of list, it would crash if we pass directly the list (differ.currentList)
        val current = ArrayList(differ.currentList)

        Collections.swap(current, from, to)
        notifyItemMoved(from,to)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int
    ) {
        val song = differ.currentList[position]
        val albumArtUri = ContentUris.withAppendedId("content://media/external/audio/albumart".toUri(), song.albumId)
        holder.binding.apply {
            Glide.with(root)
                .load(albumArtUri)
                .placeholder(R.drawable.ic_music_note)
                .error(R.drawable.ic_music_note)
                .into(imageSongItem)
            txtTitle.text = song.title ?: "Unkown song"
            txtArtist.text = song.artist ?: "Unknown artist"
            root.setOnClickListener {
                onItemClickListener?.let { it(song) }
            }
            draghandleBtn.setOnTouchListener { view, event ->
                if(event.action == MotionEvent.ACTION_DOWN){
                    dragListener.onStartDrag(holder)
                    view.performClick()
                    return@setOnTouchListener true
                }
                false
            }
        }
    }


    fun setOnItemClickListener(listener: (Song) -> Unit){
        onItemClickListener = listener
    }
}