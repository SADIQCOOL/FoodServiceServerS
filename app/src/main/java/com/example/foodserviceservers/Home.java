package com.example.foodserviceservers;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.ULocale;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import com.example.foodserviceservers.Common.Common;
import com.example.foodserviceservers.Interface.ItemClickListener;
import com.example.foodserviceservers.Model.Category;
import com.example.foodserviceservers.Service.ListenOrder;
import com.example.foodserviceservers.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    TextView txtFullName;
    private AppBarConfiguration mAppBarConfiguration;

//Firebase
FirebaseDatabase database;
DatabaseReference categories;
FirebaseStorage storage;
StorageReference storageReference;
FirebaseRecyclerAdapter<Category, MenuViewHolder>adapter;

//View
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    //ADD Nem Menu Layout
       EditText edtName;
       Button btnUpload,btnSelect;

       Category newCategory;

       Uri saveUri;

       DrawerLayout drawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Menu Management");
        setSupportActionBar(toolbar);
        //Init Firebase
        database = FirebaseDatabase.getInstance();
        categories = database.getReference("Category");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("images/");



        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                showDialog();
            }
        });
         drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        //Set for user name
        View headerView = navigationView.getHeaderView(0);
        txtFullName = (TextView)headerView.findViewById(R.id.txtFullName);
        txtFullName.setText(Common.currentUser.getName());



        //Init View

        recycler_menu = (RecyclerView)findViewById(R.id.recycler_menu);
        recycler_menu.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler_menu.setLayoutManager(layoutManager);
        loadMenu();

        //Call Service for Notification
         Intent service = new Intent(Home.this, ListenOrder.class);
         startService(service);


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                int menuId = destination.getId();


                if(menuId == R.id.nav_gallery){






                    Toast.makeText(Home.this, "Orderstatus", Toast.LENGTH_SHORT).show();
                    Intent orderintent = new Intent(Home.this,OrderStatus.class);
                    startActivity(orderintent);

                }
                else if(menuId == R.id.nav_slideshow){


                    Toast.makeText(Home.this, "SignIn", Toast.LENGTH_SHORT).show();
                    Intent sigin = new Intent(Home.this,SignIn.class);
                    sigin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(sigin);
                }


            }
        });
    }

    private void showDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Add new Category");
        alertDialog.setMessage("Please fill full information");
        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_menu_layout,null);

        edtName = add_menu_layout.findViewById(R.id.edtName);
        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);

        //Event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                chooseImage(); // let user select image from gallery and save URI of this image
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        //set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                //Here , just create  new category
                if(newCategory !=null){
                    categories.push().setValue(newCategory);
                  Snackbar.make(drawer,"New category "+newCategory.getName()+" was added",Snackbar.LENGTH_SHORT)
                          .show();

                }


            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void uploadImage() {

        if(saveUri != null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();
            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);

            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            mDialog.dismiss();
                            Toast.makeText(Home.this, "Upload !!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // set value for newCatagory if image upload & we can get download link
                                    newCategory = new Category(edtName.getText().toString(),uri.toString());


                                }
                            });
                        }
                    })
                      .addOnFailureListener(new OnFailureListener() {
                          @Override
                          public void onFailure(@NonNull Exception e) {
                              mDialog.dismiss();
                              Toast.makeText(Home.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                          }
                      })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress  = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded "+progress+"%");
                        }
                    });


        }
    }

    // press ctrl+O

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data!= null && data.getData() != null)
        {

            saveUri = data.getData();
            btnSelect.setText("Image Selected !");


        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),Common.PICK_IMAGE_REQUEST);
    }


    private void loadMenu() {

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(Category.class,
                R.layout.menu_item,
                MenuViewHolder.class,
                categories) {
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position) {


                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(Home.this).load(model.getImage())
                        .into(viewHolder.imageView);

                //for category edit

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onCLick(View view, int position, boolean isLongClick) {
                        //Send Category Id and Start new Activity
                        Intent foodList = new Intent(Home.this,FoodList.class);
                        foodList.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(foodList);

                    }
                });
            }
        };
        adapter.notifyDataSetChanged(); // Refresh data if data have changed
        recycler_menu.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    //UPDATE/DELETE
    //press ctrl + O


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE)){

            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else  if(item.getTitle().equals(Common.DELETE)){

            deleteCategory(adapter.getRef(item.getOrder()).getKey());
        }


        return super.onContextItemSelected(item);
    }

    private void deleteCategory(String key) {

        //First , we need get all food in category
        DatabaseReference foods = database.getReference("Foods");
        Query foodInCategory = foods.orderByChild("menuId").equalTo(key);
        foodInCategory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {

                    postSnapShot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        categories.child(key).removeValue();
        Toast.makeText(this, "Item deleted !!!!", Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(final String key, final Category item) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Update Category");
        alertDialog.setMessage("Please fill full information");
        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_menu_layout,null);

        edtName = add_menu_layout.findViewById(R.id.edtName);
        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);

        //set default name
        edtName.setText(item.getName());

        //Event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                chooseImage(); // let user select image from gallery and save URI of this image
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              changeImage(item);
            }
        });

        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        //set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                //Update information

                item.setName(edtName.getText().toString());
                categories.child(key).setValue(item);




            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                dialogInterface.dismiss();
            }
        });
        alertDialog.show();


    }

    private void changeImage(final Category item) {

        if(saveUri != null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();
            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);

            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            mDialog.dismiss();
                            Toast.makeText(Home.this, "Upload !!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // set value for newCatagory if image upload & we can get download link
                                   item.setImage(uri.toString());


                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(Home.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress  = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded "+progress+"%");
                        }
                    });


        }
    }
}
