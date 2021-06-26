package com.hidephoto.hidevideo.lockphoto.secretphoto.safe.testsaveimage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 1;
    String folderName="SaveImage";
    private ImageView imageView;
    private Button btnSelectImage, btnSaveImage, btnDeleteImage, btnRetrieveImage;
    private static final int GALLERY_REQUEST=1;
    Uri selectedImage;
    OutputStream outputStream;
    Bitmap bitmap;
    String imageName;
    String imageParent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        imageView=findViewById(R.id.imageFromGallery);
        btnSelectImage=findViewById(R.id.btnSelectImage);
        btnSaveImage=findViewById(R.id.btnSaveImage);
        btnDeleteImage=findViewById(R.id.btnDeleteImage);
        btnRetrieveImage=findViewById(R.id.btnRetrieveImage);

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
            }
        });
        
        btnSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });

        btnDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteImage();
            }
        });

        btnRetrieveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File outFile1 = new File(imageParent,imageName);

                String fileName=String.format("%s.txt",imageName);
                File outFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        +"/Pictures/"+folderName,fileName);

                try {
                    FileInputStream fileInputStream = new FileInputStream(outFile);
                    FileOutputStream fileOutputStream = new FileOutputStream(outFile1);
                    copyStream(fileInputStream,fileOutputStream);
                    fileOutputStream.close();
                    fileInputStream.close();
                    Toast.makeText(MainActivity.this,"Save file from image successfully",Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                outFile.delete();
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outFile1)));
            }
        });
    }

    private void deleteImage() {
        File file1= new File(getPath(selectedImage));
        file1.getName();
        file1.delete();
        Toast.makeText(MainActivity.this,"delete_image_successfully",Toast.LENGTH_SHORT).show();
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file1)));
    }

    private void saveImage() {
        imageName= getPath(selectedImage).substring(getPath(selectedImage).lastIndexOf("/")+1);
        imageParent= getPath(selectedImage).substring(0,getPath(selectedImage).lastIndexOf("/"));
        String fileName=String.format("%s.txt",imageName);
        File outFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                +"/Pictures/"+folderName,fileName);
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImage);
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);
            copyStream(inputStream,fileOutputStream);
            fileOutputStream.close();
            inputStream.close();
            Toast.makeText(MainActivity.this,imageName,Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkPermissions(){
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            return;
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            Toast.makeText(MainActivity.this,"Permission granted",Toast.LENGTH_SHORT).show();
            createDirectoty(folderName);
        }else {
            String[] permissions= {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permissions,REQUEST_PERMISSION_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,"Permission granted",Toast.LENGTH_SHORT).show();
                createDirectoty(folderName);
            }else{
                Toast.makeText(MainActivity.this,"Permission denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createDirectoty(String folderName) {
        File file=new File(Environment.getExternalStorageDirectory()+"/Pictures",folderName);
        if (!file.exists()){
            file.mkdirs();
            Toast.makeText(MainActivity.this,"Create file succesfully",Toast.LENGTH_SHORT).show();
        }else {
            return;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case GALLERY_REQUEST:
                    selectedImage = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), selectedImage);
                        imageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        Log.i("TAG", "Some exception " + e);
                    }
                    break;
            }
    }
    public String getPath(Uri uri)
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index =             cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }


    public static void copyStream(InputStream input, OutputStream output)
            throws IOException {

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    public static File changeExtension(File f, String newExtension) {
        int i = f.getName().lastIndexOf('.');
        String name = f.getName().substring(0,i);
        return new File(f.getParent(), name + newExtension);
    }

    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static Uri getImageStreamFromExternal(String imageName) {
        File externalPubPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
        );

        File picPath = new File(externalPubPath, imageName);
        Uri uri = null;
        if(picPath.exists()) {
            uri = Uri.fromFile(picPath);
        }
        return uri;
    }
}