package com.luckynineapps.stickersmaker.homefragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.luckynineapps.stickersmaker.AllStickerAdapter;
import com.luckynineapps.stickersmaker.FileItem;
import com.luckynineapps.stickersmaker.Folder;
import com.luckynineapps.stickersmaker.HomeActivity;
import com.luckynineapps.stickersmaker.ImageCropperActivity;
import com.luckynineapps.stickersmaker.ImageEditActivity;
import com.luckynineapps.stickersmaker.LocalStickerPackAdapter;
import com.luckynineapps.stickersmaker.R;
import com.luckynineapps.stickersmaker.LocalStickerAdapter;
import com.luckynineapps.stickersmaker.Tool;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@SuppressWarnings("deprecation")
public class LocalFragment extends Fragment {
    private static LocalFragment instance;
    private final int CHOOSE_STICKER = 1;
    private final int CHOOSE_TRAY_ICON = 2;
    private final int EDIT_PICTURE = 3;
    private final int CREATE_STICKER = 4;
    View v;
    HomeActivity activity;
    RecyclerView stickerPackList;
    ArrayList<Folder> folders;
    LocalStickerPackAdapter adapter;
    ProgressBar progress;
    TextView text01;
    Folder currentFolder;
    LocalStickerAdapter currentStickerAdapter;
    AllStickerAdapter currentAllStickerAdapter;
    FloatingActionButton add;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        instance = this;
        v = inflater.inflate(R.layout.fragment_home_local, container, false);
        return v;
    }

    @SuppressWarnings({"all", "deprecation"})
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (HomeActivity) getActivity();
        stickerPackList = v.findViewById(R.id.sticker_packs);
        progress = v.findViewById(R.id.progress);
        text01 = v.findViewById(R.id.text01);
        add = v.findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view0) {
                AlertDialog dialog = new AlertDialog.Builder(activity)
                        .setItems(new CharSequence[]{
                                getResources().getString(R.string.text85),
                                getResources().getString(R.string.text86)
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    // Create new sticker pack
                                    View view = LayoutInflater.from(activity).inflate(R.layout.add_pack, null);
                                    final EditText nameField = view.findViewById(R.id.name);
                                    AlertDialog dialog = new AlertDialog.Builder(activity)
                                            .setView(view)
                                            .setTitle(R.string.text22)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    String name = nameField.getText().toString();
                                                    if (name.equals("")) {
                                                        Tool.show(activity, R.string.text13);
                                                        return;
                                                    }
                                                    File dataFolder = new File(activity.getFilesDir(), "customstickers");
                                                    if (!dataFolder.exists()) {
                                                        dataFolder.mkdirs();
                                                    }
                                                    if (new File(dataFolder, name).exists()) {
                                                        Tool.show(activity, R.string.text24);
                                                        return;
                                                    }
                                                    File packFolder = new File(dataFolder, name);
                                                    packFolder.mkdirs();
                                                    Folder folder = new Folder();
                                                    folder.setPath(packFolder.getAbsolutePath());
                                                    folders.add(0, folder);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            })
                                            .setNegativeButton(R.string.text_cancel, null)
                                            .create();
                                    dialog.show();
                                } else if (i == 1) {
                                    // Create new sticker
                                    Intent i0 = new Intent(activity, ImageEditActivity.class);
                                    startActivityForResult(i0, CREATE_STICKER);
                                }
                            }
                        })
                        .create();
                dialog.show();
            }
        });
        stickerPackList.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
        stickerPackList.setItemAnimator(new DefaultItemAnimator());
        folders = new ArrayList<>();
        adapter = new LocalStickerPackAdapter(activity, folders);
        stickerPackList.setAdapter(adapter);
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... strings) {
                collectFolders();
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                stickerPackList.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                text01.setVisibility(View.GONE);
            }
        }.execute();
    }

    public void collectFolders() {
        folders.clear();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
        File rootFolder = Environment.getExternalStorageDirectory();
        ArrayList<File> folders = new ArrayList<>();
        Collections.sort(LocalFragment.this.folders, new Comparator<Folder>() {
            @Override
            public int compare(Folder folder1, Folder folder2) {
                String path1 = folder1.getPath();
                if (path1.endsWith("/")) {
                    path1 = path1.substring(0, path1.length() - 1);
                }
                String name1 = path1.substring(path1.lastIndexOf("/") + 1, path1.length());
                String path2 = folder1.getPath();
                if (path2.endsWith("/")) {
                    path2 = path2.substring(0, path2.length() - 1);
                }
                String name2 = path2.substring(path2.lastIndexOf("/") + 1, path2.length());
                return name1.compareToIgnoreCase(name2);
            }
        });
        Tool.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
        /*File myEditsFolder = new File(activity.getFilesDir(), "customstickers/myedits");
        if (!myEditsFolder.exists()) {
            myEditsFolder.mkdirs();
        }
        Folder myEdits = new Folder();
        myEdits.setPath(myEditsFolder.getAbsolutePath());
        myEdits.getFiles().addAll(getAllFilesOnly(myEditsFolder));
        if (myEdits.getFiles().size() > 3) {
            FileItem ad = new FileItem();
            ad.setType(FileItem.TYPE_AD);
            myEdits.getFiles().add(3, ad);
        }
        LocalFragment.this.folders.add(myEdits);*/
        // Collect folders from data folder > customstickers
        File stickersFolder = new File(activity.getFilesDir(), "customstickers");
        if (!stickersFolder.exists()) {
            stickersFolder.mkdirs();
        }
        for (File f : stickersFolder.listFiles()) {
            //if (!f.getAbsolutePath().equals(myEditsFolder.getAbsolutePath())) {
            Folder folder = new Folder();
            folder.setPath(f.getAbsolutePath());
            folder.getFiles().addAll(getAllFilesOnly(f));
            if (folder.getFiles().size() > 3) {
                FileItem ad = new FileItem();
                ad.setType(FileItem.TYPE_AD);
                folder.getFiles().add(3, ad);
            }
            LocalFragment.this.folders.add(folder);
            //}
        }
        // Collect folders from root
        File[] rootFiles = rootFolder.listFiles();
        if (rootFiles != null) {
            for (File f : rootFiles) {
                if (!f.isFile() && !f.getName().startsWith(".")) {
                    folders.add(f);
                }
            }
        }
        Collections.sort(folders, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                return file1.getName().compareToIgnoreCase(file2.getName());
            }
        });
        // Get files on SD Card
        {
            Folder folder = new Folder();
            folder.setPath(rootFolder.getAbsolutePath());
            ArrayList<FileItem> folderFiles = getAllFilesOnly(new File(folder.getPath()));
            if (folderFiles.size() != 0) {
                folder.getFiles().addAll(folderFiles);
                Collections.sort(folder.getFiles(), new Comparator<FileItem>() {
                    @Override
                    public int compare(FileItem file1, FileItem file2) {
                        return Long.valueOf(file2.getTime()).compareTo(Long.valueOf(file1.getTime()));
                    }
                });
                if (folderFiles.size() > 3) {
                    FileItem ad = new FileItem();
                    ad.setType(FileItem.TYPE_AD);
                    folder.getFiles().add(3, ad);
                }
                if (folderFiles.size() >= 5) {
                    FileItem viewMore = new FileItem();
                    viewMore.setType(FileItem.TYPE_VIEW_MORE);
                    viewMore.setPath(rootFolder.getAbsolutePath());
                    folder.getFiles().add(viewMore);
                }
                LocalFragment.this.folders.add(folder);
            }
        }
        for (File f : folders) {
            if (!f.getName().equals("WhatsApp")) {
                if (isContainingPicture(f)) {
                    Folder folder = new Folder();
                    folder.setPath(f.getAbsolutePath());
                    ArrayList<FileItem> folderFiles = getAllFiles(new File(folder.getPath()));
                    if (folderFiles.size() != 0) {
                        folder.getFiles().addAll(folderFiles);
                        Collections.sort(folder.getFiles(), new Comparator<FileItem>() {
                            @Override
                            public int compare(FileItem file1, FileItem file2) {
                                return Long.valueOf(file2.getTime()).compareTo(Long.valueOf(file1.getTime()));
                            }
                        });
                        if (folderFiles.size() > 3) {
                            FileItem ad = new FileItem();
                            ad.setType(FileItem.TYPE_AD);
                            folder.getFiles().add(3, ad);
                        }
                        if (folderFiles.size() >= 5) {
                            FileItem viewMore = new FileItem();
                            viewMore.setType(FileItem.TYPE_VIEW_MORE);
                            viewMore.setPath(f.getAbsolutePath());
                            folder.getFiles().add(viewMore);
                        }
                        LocalFragment.this.folders.add(folder);
                    }
                }
            }
        }
        // WhatsApp Documents
        File whatsAppDocumentsFolder = new File(rootFolder, "WhatsApp/Media/WhatsApp Documents");
        if (whatsAppDocumentsFolder.exists()) {
            Folder folder = new Folder();
            folder.setPath(whatsAppDocumentsFolder.getAbsolutePath());
            for (File f : whatsAppDocumentsFolder.listFiles()) {
                if (f.isFile()) {
                    String fileName = f.getName();
                    if (fileName.contains(".")) {
                        String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
                        if (extension.equalsIgnoreCase("jpeg")
                                || extension.equalsIgnoreCase("bmp")
                                || extension.equalsIgnoreCase("jpg")
                                || extension.equalsIgnoreCase("png")
                                || extension.equalsIgnoreCase("webp")) {
                            if (folder.getFiles().size() < 5) {
                                FileItem item = new FileItem();
                                item.setPath(f.getAbsolutePath());
                                item.setTime(f.lastModified());
                                item.setFolderPath(f.getParentFile().getAbsolutePath());
                                item.setType(FileItem.TYPE_IMAGE);
                                folder.getFiles().add(item);
                            }
                        }
                    }
                }
            }
            Collections.sort(folder.getFiles(), new Comparator<FileItem>() {
                @Override
                public int compare(FileItem file1, FileItem file2) {
                    return Long.valueOf(file2.getTime()).compareTo(Long.valueOf(file1.getTime()));
                }
            });
            if (folder.getFiles().size() > 3) {
                FileItem ad = new FileItem();
                ad.setType(FileItem.TYPE_AD);
                folder.getFiles().add(3, ad);
            }
            if (folder.getFiles().size() >= 5) {
                FileItem viewMore = new FileItem();
                viewMore.setType(FileItem.TYPE_VIEW_MORE);
                viewMore.setPath(whatsAppDocumentsFolder.getAbsolutePath());
                folder.getFiles().add(viewMore);
            }
            LocalFragment.this.folders.add(folder);
        }
        // WhatsApp Images
        File whatsAppImagesFolder = new File(rootFolder, "WhatsApp/Media/WhatsApp Images");
        if (whatsAppImagesFolder.exists()) {
            Folder folder = new Folder();
            folder.setPath(whatsAppImagesFolder.getAbsolutePath());
            for (File f : whatsAppImagesFolder.listFiles()) {
                if (f.isFile()) {
                    String fileName = f.getName();
                    if (fileName.contains(".")) {
                        String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
                        if (extension.equalsIgnoreCase("jpeg")
                                || extension.equalsIgnoreCase("bmp")
                                || extension.equalsIgnoreCase("jpg")
                                || extension.equalsIgnoreCase("png")
                                || extension.equalsIgnoreCase("webp")) {
                            if (folder.getFiles().size() < 5) {
                                FileItem item = new FileItem();
                                item.setPath(f.getAbsolutePath());
                                item.setTime(f.lastModified());
                                item.setFolderPath(f.getParentFile().getAbsolutePath());
                                item.setType(FileItem.TYPE_IMAGE);
                                folder.getFiles().add(item);
                            }
                        }
                    }
                }
            }
            Collections.sort(folder.getFiles(), new Comparator<FileItem>() {
                @Override
                public int compare(FileItem file1, FileItem file2) {
                    return Long.valueOf(file2.getTime()).compareTo(Long.valueOf(file1.getTime()));
                }
            });
            if (folder.getFiles().size() > 3) {
                FileItem ad = new FileItem();
                ad.setType(FileItem.TYPE_AD);
                folder.getFiles().add(3, ad);
            }
            if (folder.getFiles().size() >= 5) {
                FileItem viewMore = new FileItem();
                viewMore.setType(FileItem.TYPE_VIEW_MORE);
                viewMore.setPath(whatsAppImagesFolder.getAbsolutePath());
                folder.getFiles().add(viewMore);
            }
            LocalFragment.this.folders.add(folder);
        }
        // WhatsApp Profile Photos
        File whatsAppProfilePhotosFolder = new File(rootFolder, "WhatsApp/Media/WhatsApp Profile Photos");
        if (whatsAppProfilePhotosFolder.exists()) {
            Folder folder = new Folder();
            folder.setPath(whatsAppProfilePhotosFolder.getAbsolutePath());
            for (File f : whatsAppProfilePhotosFolder.listFiles()) {
                if (f.isFile()) {
                    String fileName = f.getName();
                    if (fileName.contains(".")) {
                        String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
                        if (extension.equalsIgnoreCase("jpeg")
                                || extension.equalsIgnoreCase("bmp")
                                || extension.equalsIgnoreCase("jpg")
                                || extension.equalsIgnoreCase("png")
                                || extension.equalsIgnoreCase("webp")) {
                            if (folder.getFiles().size() < 5) {
                                FileItem item = new FileItem();
                                item.setPath(f.getAbsolutePath());
                                item.setTime(f.lastModified());
                                item.setFolderPath(f.getParentFile().getAbsolutePath());
                                item.setType(FileItem.TYPE_IMAGE);
                                folder.getFiles().add(item);
                            }
                        }
                    }
                }
            }
            Collections.sort(folder.getFiles(), new Comparator<FileItem>() {
                @Override
                public int compare(FileItem file1, FileItem file2) {
                    return Long.valueOf(file2.getTime()).compareTo(Long.valueOf(file1.getTime()));
                }
            });
            if (folder.getFiles().size() > 3) {
                FileItem ad = new FileItem();
                ad.setType(FileItem.TYPE_AD);
                folder.getFiles().add(3, ad);
            }
            if (folder.getFiles().size() >= 5) {
                FileItem viewMore = new FileItem();
                viewMore.setType(FileItem.TYPE_VIEW_MORE);
                viewMore.setPath(whatsAppProfilePhotosFolder.getAbsolutePath());
                folder.getFiles().add(viewMore);
            }
            LocalFragment.this.folders.add(folder);
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private boolean isContainingPicture(File folder) {
        File[] files = folder.listFiles();
        boolean pictureAvailable = false;
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    String fileName = f.getName();
                    if (fileName.contains(".")) {
                        String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
                        if (extension.equalsIgnoreCase("jpeg")
                                || extension.equalsIgnoreCase("bmp")
                                || extension.equalsIgnoreCase("jpg")
                                || extension.equalsIgnoreCase("png")
                                || extension.equalsIgnoreCase("webp")) {
                            pictureAvailable |= true;
                        }
                    }
                } else {
                    boolean containsPicture = isContainingPicture(f);
                    pictureAvailable |= containsPicture;
                }
            }
        }
        return pictureAvailable;
    }

    private ArrayList<FileItem> getAllFiles(File folder) {
        ArrayList<FileItem> fileItems = new ArrayList<>();
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && f.length() != 0) {
                    String fileName = f.getName();
                    if (fileName.contains(".")) {
                        String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
                        if (extension.equalsIgnoreCase("jpeg")
                                || extension.equalsIgnoreCase("bmp")
                                || extension.equalsIgnoreCase("jpg")
                                || extension.equalsIgnoreCase("png")
                                || extension.equalsIgnoreCase("webp")) {
                            if (fileItems.size() < 5) {
                                FileItem item = new FileItem();
                                item.setType(FileItem.TYPE_IMAGE);
                                item.setPath(f.getAbsolutePath());
                                item.setTime(f.lastModified());
                                fileItems.add(item);
                            }
                        }
                    }
                } else {
                    String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                    String folderPath = rootPath + "/Android/data/com.dn.whatsappstickers";
                    String whatsAppPath = rootPath + "/WhatsApp";
                    String privateAppDataPath = rootPath + "/Android/data/com.dn.whatsappstickers";
                    if (!f.getAbsolutePath().equals(folderPath)
                            && !f.getAbsolutePath().equals(whatsAppPath)
                            && !f.getAbsolutePath().equals(privateAppDataPath)) {
                        ArrayList<FileItem> folderFileItems = getAllFiles(f);
                        for (FileItem item : folderFileItems) {
                            if (fileItems.size() < 5) {
                                fileItems.add(item);
                            }
                        }
                    }
                }
            }
        }
        return fileItems;
    }

    private ArrayList<FileItem> getAllFilesOnly(File folder) {
        ArrayList<FileItem> fileItems = new ArrayList<>();
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && f.length() != 0) {
                    String fileName = f.getName();
                    if (fileName.contains(".")) {
                        String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
                        if (extension.equalsIgnoreCase("jpeg")
                                || extension.equalsIgnoreCase("bmp")
                                || extension.equalsIgnoreCase("jpg")
                                || extension.equalsIgnoreCase("png")
                                || extension.equalsIgnoreCase("webp")) {
                            if (fileItems.size() < 5) {
                                FileItem item = new FileItem();
                                item.setType(FileItem.TYPE_IMAGE);
                                item.setPath(f.getAbsolutePath());
                                item.setTime(f.lastModified());
                                fileItems.add(item);
                            }
                        }
                    }
                }
            }
        }
        return fileItems;
    }

    public void addSticker(Folder folder, LocalStickerAdapter adapter) {
        currentFolder = folder;
        currentStickerAdapter = adapter;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, activity.getResources().getString(R.string.text21)), CHOOSE_STICKER);
    }

    public void changeTray(Folder folder, LocalStickerAdapter adapter) {
        currentFolder = folder;
        currentStickerAdapter = adapter;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, activity.getResources().getString(R.string.text21)), CHOOSE_TRAY_ICON);
    }

    public void deleteStickerPack(final Folder folder, final int index) {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setMessage("Are you sure you want to delete this sticker pack?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        folders.remove(index);
                        adapter.notifyDataSetChanged();
                        new File(folder.getPath()).delete();
                    }
                })
                .setNegativeButton("No", null)
                .create();
        dialog.show();
    }

    public void editImage(LocalStickerAdapter adapter, FileItem item) {
        currentStickerAdapter = adapter;
        currentAllStickerAdapter = null;
        Intent i = new Intent(activity, ImageEditActivity.class);
        i.putExtra("image_path", item.getPath());
        startActivityForResult(i, EDIT_PICTURE);
    }

    public void editImage(AllStickerAdapter adapter, FileItem item) {
        currentStickerAdapter = null;
        currentAllStickerAdapter = adapter;
        Intent i = new Intent(activity, ImageCropperActivity.class);
        i.putExtra("image_path", item.getPath());
        startActivityForResult(i, EDIT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CHOOSE_STICKER || requestCode == CHOOSE_TRAY_ICON) {
                String filePath = Tool.getDataPath(activity, data.getData());
                if (filePath.endsWith("/")) {
                    filePath = filePath.substring(0, filePath.length() - 1);
                }
                String fileName = filePath.substring(filePath.lastIndexOf("/"), filePath.length());
                String dstPath = currentFolder.getPath() + fileName;
                File dstFile = new File(dstPath);
                Tool.copyFile(filePath, dstPath);
                dstFile.setLastModified(System.currentTimeMillis());
                FileItem newItem = new FileItem();
                newItem.setType(FileItem.TYPE_IMAGE);
                newItem.setFolderPath(currentFolder.getPath());
                newItem.setTime(new File(filePath).lastModified());
                newItem.setPath(dstPath);
                currentFolder.getFiles().add(0, newItem);
                if (currentFolder.getFiles().size() > 3) {
                    FileItem ad = new FileItem();
                    ad.setType(FileItem.TYPE_AD);
                    currentFolder.getFiles().add(3, ad);
                }
                Tool.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (currentStickerAdapter != null) {
                            currentStickerAdapter.notifyDataSetChanged();
                        } else if (currentAllStickerAdapter != null) {
                            currentAllStickerAdapter.notifyDataSetChanged();
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
            } else if (requestCode == EDIT_PICTURE) {
                String imagePath = data.getStringExtra("image_path");
                FileItem newItem = new FileItem();
                newItem.setPath(imagePath);
                newItem.setTime(System.currentTimeMillis());
                newItem.setFolderPath(new File(imagePath).getParentFile().getAbsolutePath());
                newItem.setType(FileItem.TYPE_IMAGE);
                /*if (currentStickerAdapter != null) {
                    currentStickerAdapter.files.add(1, newItem);
                    if (currentStickerAdapter.files.size() > 6) {
                        for (int i = 5; i < currentStickerAdapter.files.size() - 1; i++) {
                            currentStickerAdapter.files.remove(i);
                        }
                    }
                    currentStickerAdapter.notifyDataSetChanged();
                } else if (currentAllStickerAdapter != null) {
                    currentAllStickerAdapter.files.add(1, newItem);
                    if (currentAllStickerAdapter.files.size() > 6) {
                        for (int i = 5; i < currentAllStickerAdapter.files.size() - 1; i++) {
                            currentAllStickerAdapter.files.remove(i);
                        }
                    }
                    currentAllStickerAdapter.notifyDataSetChanged();
                }*/
                boolean myEditsFolderAlreadyAdded = false;
                int index = 0;
                for (Folder f : folders) {
                    if (f.getPath().equals(new File(activity.getFilesDir(), "customstickers/myedits").getAbsolutePath())) {
                        f.getFiles().add(newItem);
                        if (f.getFiles().size() > 3) {
                            FileItem ad = new FileItem();
                            ad.setType(FileItem.TYPE_AD);
                            f.getFiles().add(3, ad);
                        }
                        adapter.notifyItemChanged(index);
                        myEditsFolderAlreadyAdded = true;
                        break;
                    }
                    index++;
                }
                if (!myEditsFolderAlreadyAdded) {
                    Folder f = new Folder();
                    f.getFiles().add(newItem);
                    f.setPath(new File(activity.getFilesDir(), "customstickers/myedits").getAbsolutePath());
                    folders.add(0, f);
                    adapter.notifyDataSetChanged();
                }
                int totalPictureEdits = Tool.read(activity, "total_edits", 0);
                totalPictureEdits++;
                Tool.save(activity, "total_edits", totalPictureEdits);
            } else if (requestCode == CREATE_STICKER) {
                String imagePath = data.getStringExtra("image_path");
                // Check if folders list contains path to "myedits" in data folder
                File myEditsFolder = new File(activity.getFilesDir(), "customstickers/myedits");
                if (!myEditsFolder.exists()) {
                    myEditsFolder.mkdirs();
                }
                boolean alreadyContains = false;
                Folder folder0 = null;
                for (Folder folder : folders) {
                    if (folder.getPath().equals(myEditsFolder.getAbsolutePath())) {
                        alreadyContains = true;
                        folder0 = folder;
                        break;
                    }
                }
                if (alreadyContains) {
                    // Folders list already contains path to "myedits"
                    FileItem item = new FileItem();
                    item.setType(FileItem.TYPE_IMAGE);
                    item.setPath(imagePath);
                    item.setFolderPath(myEditsFolder.getAbsolutePath());
                    folder0.getFiles().add(item);
                    adapter.notifyDataSetChanged();
                } else {
                    // Create new sticker pack/folder called "myedits"
                    Folder folder = new Folder();
                    folder.setPath(myEditsFolder.getAbsolutePath());
                    FileItem item = new FileItem();
                    item.setType(FileItem.TYPE_IMAGE);
                    item.setPath(imagePath);
                    item.setFolderPath(myEditsFolder.getAbsolutePath());
                    folder.getFiles().add(item);
                    folders.add(folder);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    public static LocalFragment getInstance() {
        return instance;
    }
}
