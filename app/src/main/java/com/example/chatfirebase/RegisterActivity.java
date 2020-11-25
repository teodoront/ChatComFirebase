package com.example.chatfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;


public class RegisterActivity extends AppCompatActivity {

    private EditText mEditUserName;
    private EditText mEditEmail;
    private EditText mEditPassword;
    private Button mBtnEnterSave;
    private Button mBtnSelectedPhoto;
    private CircleImageView mImgPhoto;


    private Uri mSelectedUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_register);


        mEditUserName = findViewById(R.id.edit_username);
        mEditEmail = findViewById(R.id.edit_email);
        mEditPassword = findViewById(R.id.edit_password);
        mBtnEnterSave = findViewById(R.id.btn_enter_save);
        mBtnSelectedPhoto = findViewById(R.id.btn_selected_photo);
        mImgPhoto = findViewById(R.id.img_photo);

        mBtnEnterSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
                Log.i("teste", "funciona");
            }
        });



        mBtnSelectedPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectPhoto();
            }
        });

    }

    private void createUser() {
        String name = mEditUserName.getText().toString();
        String email = mEditEmail.getText().toString();
        String password = mEditPassword.getText().toString();

        Validation validation = new Validation();
     /*   validation.isEmptyFields(name, email, password);
        validation.isEmailValid(email);
        validation.isPswValid(password);*/

        //Validando se todos campos estão preenchidos
        if (validation.isEmptyFields(name, email, password)) {
            Log.i("validação de campos", "o idiota tá tentando salvar com algum campo vazio");
            Toast.makeText(this, "Os campos nome, email e senha precisam estar todos preenchidos", Toast.LENGTH_LONG).show();
            return;
        }
        //Validando se o email está no padrão
        if (!validation.isEmailValid(email)) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("Alerta");
            dlg.setMessage("O e-mail está fora do padrão!!!!");
            dlg.setNeutralButton("Ok", null);
            dlg.show();
            //Isso é para o cursor voltar no campo email depois de validado
            mEditEmail.requestFocus();
        }
        //Validando se o senha tem no mínimo 6 carecteres
        if (!validation.isPswValid(password)) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("Alerta");
            dlg.setMessage("A quantidade mínima é 6 caracteres para senha!!!");
            dlg.setNeutralButton("Ok", null);
            dlg.show();
            mEditEmail.requestFocus();
        }
        //Salva o usuário no Firebase
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(/**implementar um objeto anonimo**/new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i("teste", task.getResult().getUser().getUid());

                            saveUserInFirebase();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //para ver alguma falha ao salvar usuário no firebase
                        Log.i("teste_salvar_firebase", e.getMessage());
                    }
                });


    }

    //Esse metodo pega a seleção da imagem da galeria. Passa o dado que busquei de um outro serviço ou intenção. onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            mSelectedUri = data.getData();
            //Instância de bitmap
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mSelectedUri);
                mImgPhoto.setImageDrawable(new BitmapDrawable(bitmap));
                //Pra exibir a foto e esconder o botão que está na frente. O botão ainda existe, pega o evento de touch, mas está oculto.
                mBtnSelectedPhoto.setAlpha(0);
            } catch (IOException e) {
                e.printStackTrace(); //log p mostrar no logCat
            }
        }
    }

    private void SelectPhoto() {
        //cria uma nova intenção p acessar o diretório de fotos. Cria intenção também para um activity sobrepor a outra;
        Intent intent = new Intent(Intent.ACTION_PICK); //obtem a foto
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

    //O ideal era esses metodos serem tratados em outras classes, não tratar regras de negócio na activity Depois refatorar.

    private void saveUserInFirebase() {
        //Vai gerar uma hash aleatória p termos o nome do arquivo de forma unica
        String filename = UUID.randomUUID().toString();
        final StorageReference ref = FirebaseStorage.getInstance().getReference("/images" + filename);
        ref.putFile(mSelectedUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //trata o sucesso
                        //Vou pegar de volta a imagem depois que ela subir para o firebase
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //se deu certo, vai imprimir a url publica do firebase no Logcat
                                Log.i("UrlFirebase", uri.toString());

                                String uid = FirebaseAuth.getInstance().getUid();
                                String username = mEditUserName.getText().toString();
                                String profileUrl = uri.toString();

                                User user = new User(uid, username, profileUrl);

                           FirebaseFirestore.getInstance().collection("users")
                                        .add(user)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                //vai escutar o sucesso

                                                Log.i("Teste", documentReference.getId());
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //vai escutar a falha

                                                Log.i("Teste", e.getMessage());
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //trata a falha
                        Log.e("ErrorSaveFirebase", e.getMessage(), e);
                    }
                });
    }

}