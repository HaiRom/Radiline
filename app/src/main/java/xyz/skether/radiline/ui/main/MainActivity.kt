package xyz.skether.radiline.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.transaction
import kotlinx.android.synthetic.main.activity_main.*
import xyz.skether.radiline.R
import xyz.skether.radiline.domain.Track
import xyz.skether.radiline.service.PlaybackService
import xyz.skether.radiline.ui.base.BaseActivity
import xyz.skether.radiline.ui.base.LayoutId
import xyz.skether.radiline.utils.logError

@LayoutId(R.layout.activity_main)
class MainActivity : BaseActivity(), ServiceConnection {

    private var playbackService: PlaybackService? = null
    private val playbackListener = PlaybackListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)

        playButton.setOnClickListener {
            playbackService?.apply {
                if (isPlaying) stopPlayback() else playStation()
            }
        }

        navigation.setOnNavigationItemSelectedListener(this::onNavigationItemSelected)
        if (savedInstanceState == null) {
            navigation.selectedItemId = navigation.selectedItemId
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(PlaybackService.bindIntent(this), this, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        if (playbackService != null) {
            playbackService?.removeListener(playbackListener)
            unbindService(this)
        }
        super.onStop()
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (currentFragment == null
            || currentFragment !is GenresFragment
            || !currentFragment.closeGenre()
        ) {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_previous -> {
            playbackService?.playPreviousStation()
            true
        }
        R.id.action_about -> {
            showAboutDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        binder as PlaybackService.LocalBinder
        playbackService = binder.service.also {
            it.addListener(playbackListener)
            updatePlayButton(it.isPlaying)
            updateTitle(it.currentTrack)
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        playbackService = null
    }

    private fun onNavigationItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.nav_genres -> openFragment(GenresFragment())
        R.id.nav_top -> openFragment(TopStationsFragment())
        R.id.nav_search -> openFragment(SearchFragment())
        else -> false
    }

    private fun openFragment(fragment: Fragment): Boolean {
        supportFragmentManager.transaction {
            replace(R.id.fragmentContainer, fragment)
        }
        return true
    }

    private fun updateTitle(track: Track?) {
        toolbar_layout.title = track?.title ?: getString(R.string.app_name)
    }

    private fun updatePlayButton(isPlaying: Boolean) {
        if (isPlaying) {
            changePlayButtonIcon(R.drawable.ic_stop_white_24dp)
        } else {
            changePlayButtonIcon(R.drawable.ic_play_arrow_white_24dp)
        }
    }

    private fun changePlayButtonIcon(@DrawableRes resId: Int) {
        playButton.setImageResource(resId)
        playButton.hide() // because of this bug
        playButton.show() // https://stackoverflow.com/questions/51919865/disappearing-fab-icon-on-navigation-fragment-change
    }

    private fun showAboutDialog() {
        val dialog = AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_info_outline_black_24dp)
            .setTitle(R.string.about_title)
            .setMessage(R.string.about_message)
            .setPositiveButton(R.string.ok, null)
            .create()
        dialog.show()
        dialog.findViewById<TextView>(android.R.id.message)!!.movementMethod = LinkMovementMethod.getInstance()
    }

    private inner class PlaybackListener : PlaybackService.Listener {

        override fun onStateChanged(isPlaying: Boolean) {
            updatePlayButton(isPlaying)
        }

        override fun onTrackChanged(track: Track?) {
            updateTitle(track)
        }

        override fun onError(exception: Exception) {
            showSnackbar(R.string.error_play_station)
            logError(exception)
        }

    }

}
