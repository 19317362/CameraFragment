CameraFragment
==============

a sample implementation of camera fragment.

the goal of CameraFragment is creating a fragment which can attach to activities and dialogs with any aspect ratio.

#usage

just put this fragment on your layout


```
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity" >

    <fragment
        android:id="@+id/CameraFragment"
        android:name="jp.co.mongolian.android.camerafragment.CameraFragment"
        android:layout_width="240dp"
        android:layout_height="240dp"
        android:layout_centerHorizontal="true"
        android:layout_alignParentTop="true"
        android:layout_marginTop="50dp"
        tools:layout="@layout/fragment_camera" />

</RelativeLayout>

```
