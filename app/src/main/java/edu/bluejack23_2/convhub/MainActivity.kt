package edu.bluejack23_2.convhub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var currentlyExpandedLayout : LinearLayout? = null;
    private var currentlyExpandedTextView : TextView? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Disable status bar
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        // Obtain references to the layouts and text views
        val homeLayout: LinearLayout = findViewById(R.id.homeLayout)
        val homeTxt: TextView = findViewById(R.id.homeTxt)

        val scheduleLayout: LinearLayout = findViewById(R.id.scheduleLayout)
        val scheduleTxt: TextView = findViewById(R.id.scheduleTxt)

        val taskLayout: LinearLayout = findViewById(R.id.tasksLayout)
        val taskTxt: TextView = findViewById(R.id.tasksTxt)

        val profileLayout: LinearLayout = findViewById(R.id.profileLayout)
        val profileTxt: TextView = findViewById(R.id.profileTxt);

        setActiveNav(homeLayout, homeTxt);

        homeLayout.setOnClickListener { toggleView(homeLayout, homeTxt) }
        scheduleLayout.setOnClickListener { toggleView(scheduleLayout, scheduleTxt) }
        taskLayout.setOnClickListener { toggleView(taskLayout, taskTxt) }
        profileLayout.setOnClickListener { toggleView(profileLayout, profileTxt) }
    }

    private fun setActiveNav(layout: LinearLayout, textView: TextView) {
        currentlyExpandedLayout = layout;
        currentlyExpandedTextView = textView;
    }


    private fun toggleView(layout: LinearLayout, textView: TextView) {
        if (layout == currentlyExpandedLayout) return

        if (currentlyExpandedLayout != null) {
            currentlyExpandedLayout!!.setBackgroundResource(android.R.color.transparent)
        }
        if (currentlyExpandedTextView != null) {
            currentlyExpandedTextView!!.visibility = View.GONE
        }

        textView.visibility = View.VISIBLE
        layout.setBackgroundResource(R.drawable.round_back_home)

        setActiveNav(layout, textView)
    }
}