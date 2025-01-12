package com.cpen321.circuitsolver.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.ui.draw.DrawActivity;
import com.cpen321.circuitsolver.util.BaseActivity;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;
import com.cpen321.circuitsolver.util.ImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import static com.cpen321.circuitsolver.util.Constants.REQUEST_TAKE_PHOTO;

public class HomeActivity extends BaseActivity {
    private static final String TAG = "HomeActivity";
    private static final String APP_NAME = "com.cpen321.circuitsolver";

    private LinearLayout savedCircuitsScroll;

    private ArrayList<CircuitProject> circuitProjects = new ArrayList<>();
    private CircuitProject candidateProject;

    private static String selectedTag = null;

    private FloatingActionButton processingFab;
    private View cameraFab;
    private View loadFab;
    private View drawFab;
    private FloatingActionButton deleteFab;

    //private static ArrayList<Activity> activities=new ArrayList<Activity>();



    private View.OnClickListener thumbnailListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LinearLayout parentView = (LinearLayout) view.getParent();

            for(int i=0; i < parentView.getChildCount(); i++) {
                ImageView imgView = (ImageView) parentView.getChildAt(i);
                imgView.setColorFilter(null);
            }

            if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                if (imageView.getTag() == HomeActivity.selectedTag){
                    imageView.setColorFilter(null);
                    HomeActivity.this.setSelectedTag(null);
                } else {
                    imageView.setColorFilter(Color.argb(100, 112, 17, 19));
                    HomeActivity.this.setSelectedTag((String) imageView.getTag());
                }
            }
        }
    };

    public void setSelectedTag(String selectedTag) {
        if (selectedTag == null){
            this.deleteFab.hide();
            this.processingFab.hide();
        } else {
            this.deleteFab.show();
            this.processingFab.show();
        }
        HomeActivity.selectedTag = selectedTag;
    }

    //*********
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_action_button, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_moreinfo:
                CardView helpCard = (CardView) findViewById(R.id.help_card);
                if (helpCard.getVisibility() == View.VISIBLE)
                    helpCard.setVisibility(View.INVISIBLE);
                else
                    helpCard.setVisibility(View.VISIBLE);
            default:
               return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //activities.add(this);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.cameraFab =  findViewById(R.id.capture_fab);
        this.loadFab =  findViewById(R.id.load_fab);
        this.drawFab =  findViewById(R.id.draw_fab);
        this.processingFab = (FloatingActionButton) findViewById(R.id.processing_fab);
        this.deleteFab = (FloatingActionButton) findViewById(R.id.delete_fab);
        this.savedCircuitsScroll = (LinearLayout) findViewById(R.id.saved_circuits_scroll);
        //this.exampleCircuitScroll = (LinearLayout) findViewById(R.id.example_circuits_scroll);
        this.updateSavedCircuits();
        //this.loadExamples();

        this.checkNecessaryPermissions();

        this.cameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeActivity.this.checkNecessaryPermissions();
                HomeActivity.this.dispatchTakePictureIntent();
            }
        });

        this.loadFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeActivity.this.checkNecessaryPermissions();
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, Constants.RESULT_LOAD_IMAGE);
            }
        });

        this.drawFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent displayIntent = new Intent(HomeActivity.this, DrawActivity.class);
                startActivity(displayIntent);
                finish();
            }
        });

        this.processingFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if (HomeActivity.selectedTag == null)
                        return;
                File circuitFolder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), HomeActivity.selectedTag);
                // try-catch: prevents the case where the user may end the app during the processingActivity
                boolean notCorrupt = true;
                try {
                    CircuitProject circuitProject = new CircuitProject(circuitFolder);
                    String circStr = circuitProject.getCircuitText();
                }
                catch(Exception e){
                    notCorrupt = false;
                    CircuitProject projToDelete = new CircuitProject(circuitFolder);
                    projToDelete.deleteFileSystem();
                    if (projToDelete.deleteFileSystem()) {
                        HomeActivity.this.setSelectedTag(null);
                        Toast.makeText(
                                HomeActivity.this,
                                "Project Corrupt. Deleted",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                                HomeActivity.this,
                                "Project Corrupt. Attempt to Delete Failed.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                    HomeActivity.this.updateSavedCircuits();
                }
                if(notCorrupt){
                    Intent displayIntent = new Intent(HomeActivity.this, DrawActivity.class);
                    displayIntent.putExtra(Constants.CIRCUIT_PROJECT_FOLDER, circuitFolder.getAbsolutePath());
                    startActivity(displayIntent);
                }
                finish();
            }
        });

        this.deleteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HomeActivity.selectedTag == null)
                    return;
                if (HomeActivity.selectedTag.contains("example"))
                    return;
                File circuitFolder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), HomeActivity.selectedTag);
                CircuitProject projToDelete = new CircuitProject(circuitFolder);
                if (projToDelete.deleteFileSystem()) {
                    HomeActivity.this.setSelectedTag(null);
                    Toast.makeText(
                            HomeActivity.this,
                            "Project Deleted",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                            HomeActivity.this,
                            "Failed to delete project.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
                HomeActivity.this.updateSavedCircuits();
            }
        });
    }

    // TAKEN FROM OFFICIAL ANDROID DEVELOPERS PAGE (NOT MY OWN CODE)
    private void dispatchTakePictureIntent() {
        this.candidateProject = new CircuitProject(ImageUtils.getTimeStamp(),
                getExternalFilesDir(Environment.DIRECTORY_PICTURES));

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = this.candidateProject.generateOriginalImageFile();

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        APP_NAME,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
            else {
                System.out.println("photo file is null");
            }
        }
    }
    // END OF CODE TAKEN FROM OFFICIAL ANDROID DEVELOPERS PAGE

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean continueToProcessing = false;
        if (requestCode == Constants.RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            continueToProcessing = true;
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap bm = BitmapFactory.decodeFile(picturePath);

            this.candidateProject = new CircuitProject(ImageUtils.getTimeStamp(),
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            // Create the File where the photo should go
            File photoFile = this.candidateProject.generateOriginalImageFile();
            if (photoFile == null) {
                Log.d(TAG,
                        "Error creating media file, check storage permissions: ");// e.getMessage());
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(photoFile);
                bm.compress(Bitmap.CompressFormat.JPEG, Constants.COMPRESSION_QUALITY, fos);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

        }
        else if(requestCode == REQUEST_TAKE_PHOTO){
            continueToProcessing = true;
        }

        if(continueToProcessing) {
            Intent analysisIntent = new Intent(getApplicationContext(), ProcessingActivity.class);
            analysisIntent.putExtra(Constants.CIRCUIT_PROJECT_FOLDER, this.candidateProject.getFolderPath());
            startActivity(analysisIntent);
            finish();
        }
    }

    private void deleteFolderOrFile(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles())
                this.deleteFolderOrFile(child);
            file.delete();
        } else {
            file.delete();
        }
    }

    public void updateSavedCircuits(){
        File imageStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] circuitProjects = imageStorageDir.listFiles();
        this.savedCircuitsScroll.removeAllViews();
        this.circuitProjects.clear();

        for (File project : circuitProjects) {
            if (project.isDirectory()){
                if (project.listFiles().length == 0)
                    this.deleteFolderOrFile(project);
                else {
                    this.circuitProjects.add(new CircuitProject(project));
                }
            }
        }

        for (CircuitProject circuitProject : this.circuitProjects) {
            ImageView newImage = new ImageView(getApplicationContext());
            newImage.setTag(circuitProject.getFolderID());
            newImage.setPadding(10, 10, 10, 10);
            try {
                newImage.setImageBitmap(circuitProject.getThumbnail());
            } catch (NullPointerException ex) {
                newImage.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                        R.drawable.not_found));
            }
            newImage.setOnClickListener(this.thumbnailListener);
            this.savedCircuitsScroll.addView(newImage);
        }
    }

    // USED FOR TESTING PURPOSES. UNKNOWN IF IT IS WISE TO HAVE THESE SETTERS. USE WITH CAUTION
    // USED TO MOCK INITIAL CAMERA/LOAD FROM FILE INPUT
    public ArrayList<CircuitProject> getCircuitProjects(){
        return circuitProjects;
    }
    public void setCandidateProject(CircuitProject candidateProject){
        this.candidateProject = candidateProject;
    }
    public void setCircuitProjects(ArrayList<CircuitProject> circuitProjects){
        this.circuitProjects = circuitProjects;
    }
    public void addCircuitProject(CircuitProject proj){
        this.circuitProjects.add(proj);
    }

//    public void addScrollItem(CircuitProject candidateProject){
//        ImageView newImage = new ImageView(getApplicationContext());
//        newImage.setTag(candidateProject.getFolderID());
//        newImage.setPadding(10, 10, 10, 10);
//        try {
//            newImage.setImageBitmap(candidateProject.getThumbnail());
//        } catch (NullPointerException ex) {
//            newImage.setImageBitmap(BitmapFactory.decodeResource(getResources(),
//                    R.drawable.not_found));
//        }
//        newImage.setOnClickListener(this.thumbnailListener);
//        this.savedCircuitsScroll.addView(newImage);
//    }
}
