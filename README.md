# SwipeMenuLayout
A slide swipe menu build with kotlin which can use customized layout acts on recycleView / listView .etc item or any other views.

[![](https://www.jitpack.io/v/baybomax/SwipeMenuLayout.svg)](https://www.jitpack.io/#baybomax/SwipeMenuLayout)

![](https://github.com/baybomax/SwipeMenuLayout/blob/master/app/src/main/res/raw/sml_case.gif)

# How to use

	//all projects
	allprojects {
		repositories {
			...
			jcenter()
			maven { url "https://jitpack.io" }
		}
	}

	or
	
	//application
	repositories {
    	...
    	maven {
			url "https://jitpack.io"
    	}
	}

	dependencies {
    	...
    	implementation 'com.github.baybomax:SwipeMenuLayout:1.1.2'
	}

# Notice

	If you want use this layout acts on multiple list item, recommended that conjunction with another 
	repository named MultiRecycleViewAdapter
[![](https://www.jitpack.io/v/baybomax/MultiRecycleViewAdapter.svg)](https://www.jitpack.io/#baybomax/MultiRecycleViewAdapter)

# Example

	You can simple directly use this layout as customized root viewgroup, such as follow
	
	<?xml version="1.0" encoding="utf-8"?>
	<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    		xmlns:app="http://schemas.android.com/apk/res-auto"
    		xmlns:tools="http://schemas.android.com/tools"
    		android:layout_width="match_parent"
    		android:layout_height="wrap_content"
    		android:orientation="vertical"
    		android:clickable="true"
    		tools:ignore="KeyboardInaccessibleWidget"
    		>

    		<com.android.db.swipemenulayout.SwipeMenuLayout
        		android:id="@+id/sml"
        		android:layout_width="match_parent"
        		android:layout_height="wrap_content"
				
				app:leftView="@+id/left"
				app:contentView="@+id/content"
				app:rightView="@+id/right"
       			>

        		<LinearLayout
            		android:id="@+id/left"
            		android:layout_width="100dp"
            		android:layout_height="wrap_content"
            		android:padding="20dp"

            		android:background="@android:color/holo_blue_dark"
            		android:orientation="horizontal"
            		tools:ignore="NewApi"
            		>

					<TextView
                		android:layout_width="wrap_content"
                		android:layout_height="wrap_content"
                		android:clickable="true"
                		android:text="share"
                		tools:ignore="HardcodedText"
                		/>

        		</LinearLayout>

        		<LinearLayout
            		android:id="@+id/content"
            		android:layout_width="match_parent"
            		android:layout_height="wrap_content"
            		android:padding="20dp"

            		android:background="#cccccc"
            		android:orientation="vertical"
            		>

            		<TextView
                		android:layout_width="wrap_content"
                		android:layout_height="wrap_content"
                		android:text="customized"
                		tools:ignore="HardcodedText"
                		/>

        		</LinearLayout>

        		<LinearLayout
            		android:id="@+id/right"
            		android:layout_width="wrap_content"
            		android:layout_height="wrap_content"

            		android:background="@android:color/holo_red_light"
            		android:orientation="horizontal"
            		tools:ignore="NewApi"
            		>

            		<TextView
                		android:layout_width="wrap_content"
                		android:layout_height="wrap_content"
                		android:padding="20dp"

                		android:background="@android:color/holo_blue_bright"
                		android:clickable="true"
                		android:text="delete"
                		tools:ignore="HardcodedText,NewApi"
                		/>

            		<TextView
                		android:id="@+id/collect"
                		android:layout_width="wrap_content"
                		android:layout_height="wrap_content"
                		android:padding="20dp"

                		android:background="@android:color/holo_orange_dark"
                		android:clickable="true"
                		android:text="collect"
                		tools:ignore="HardcodedText,NewApi"
                		/>

        		</LinearLayout>
    		</com.android.db.swipemenulayout.SwipeMenuLayout>

		</LinearLayout>

		1.define left/content/right view id.
			app:leftView="@+id/left"
			app:contentView="@+id/content"
			app:rightView="@+id/right"
			
		2.left/content/right view id must be same as define.

# Concluding

	If you like, you can have a star, or any questions please give me issues.
	Also can inquire into kotlin or others with me if you interest in what.
