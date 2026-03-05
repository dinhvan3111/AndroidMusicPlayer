package com.example.musicplayerapp.ui.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayerapp.R
import com.example.musicplayerapp.databinding.FragmentMusicLibraryBinding
import com.example.musicplayerapp.models.Song
import com.example.musicplayerapp.repositories.MusicRepository
import com.example.musicplayerapp.ui.viewmodels.MusicLibraryViewModel
import com.example.musicplayerapp.ui.MusicLibraryViewModelProviderFactory
import com.example.musicplayerapp.ui.PlayerActivity
import com.example.musicplayerapp.ui.SongAdapter
import com.example.musicplayerapp.utils.Resource
import kotlinx.coroutines.launch

class MusicLibraryFragment : Fragment(R.layout.fragment_music_library){

    lateinit var viewModel : MusicLibraryViewModel
    lateinit var songAdapter: SongAdapter
    lateinit var binding: FragmentMusicLibraryBinding
    val TAG = "MusicLibraryFragment"
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ isGranted: Boolean ->
        if(isGranted){
            loadDownloadedSong();
        }
        else{
            Toast.makeText(
                requireContext(),
                "Permission denied",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMusicLibraryBinding.bind(view)
        val appContext = requireContext().applicationContext
        val repository = MusicRepository(appContext)
        val viewModelProviderFactory = MusicLibraryViewModelProviderFactory(repository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(MusicLibraryViewModel::class.java)
        setUpRecyclerView()
        checkPermissionAndLoadSongs()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.downloadedMusicList.collect { value ->
                    when(value){
                        is Resource.Success -> {
                            songAdapter.differ.submitList(value.data)
                        }
                        is Resource.Error -> {
                            value.message?.let {
                                Log.e(TAG, "An error occured: $it")
                            }
                        }
                        is Resource.Loading -> {

                        }
                    }
                }
            }
        }
    }

    private fun setUpRecyclerView(){
        songAdapter = SongAdapter().apply {
            setOnItemClickListener{selectedSong -> onItemClick(selectedSong)}
        }
        binding.rvListSong.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }
    private fun checkPermissionAndLoadSongs(){
        var permission : String
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            permission = Manifest.permission.READ_MEDIA_AUDIO
        }
        else{
            permission = Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when{
            ContextCompat.checkSelfPermission(requireContext(),permission)
                    == PackageManager.PERMISSION_GRANTED -> {
                        loadDownloadedSong()
                    }
            shouldShowRequestPermissionRationale(permission) -> {

            }

            else ->{
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun showPermissionDialog(permission: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("We need access to your music files.")
            .setPositiveButton("OK") { _, _ ->
                requestPermissionLauncher.launch(permission)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadDownloadedSong(){
        viewModel.getListDownloadedMusic()
    }

    private fun onItemClick(selectedSong: Song){
        val intent = Intent(requireContext(), PlayerActivity::class.java)
        intent.putParcelableArrayListExtra("songList", ArrayList<Song>(songAdapter.differ.currentList))
        intent.putExtra("position", songAdapter.differ.currentList.indexOf(selectedSong))
        startActivity(intent)
    }
}