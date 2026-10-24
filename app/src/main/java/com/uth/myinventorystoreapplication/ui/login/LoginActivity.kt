package com.uth.myinventorystoreapplication.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uth.myinventorystoreapplication.MainActivity
import com.uth.myinventorystoreapplication.R
import com.uth.myinventorystoreapplication.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbRemember: CheckBox
    private lateinit var btnLogin: Button
    private lateinit var btnQuickDemo: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        // Si ya hay una sesión activa, ir directo a MainActivity
        if (sessionManager.isLoggedIn()) {
            openMainActivity()
            return
        }

        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        cbRemember = findViewById(R.id.cbRemember)
        btnLogin = findViewById(R.id.btnLogin)
        btnQuickDemo = findViewById(R.id.btnQuickDemo)

        btnLogin.setOnClickListener {
            performLogin()
        }

        btnQuickDemo.setOnClickListener {
            etUsername.setText("admin")
            etPassword.setText("admin123")
            performLogin()
        }
    }

    private fun performLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (username.isEmpty()) {
            etUsername.error = "Ingrese su usuario"
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Ingrese su contraseña"
            return
        }

        // Validación simple de usuario administrador (ej: admin / admin123)
        if (username.equals("admin", ignoreCase = true) && password == "admin123") {
            if (cbRemember.isChecked) {
                sessionManager.createLoginSession(username, "Administrador de Pulpería")
            }
            Toast.makeText(this, "¡Bienvenido, Administrador!", Toast.LENGTH_SHORT).show()
            openMainActivity()
        } else if (username.isNotBlank() && password.length >= 4) {
            // Permitir inicio de sesión flexible para otros usuarios de prueba
            if (cbRemember.isChecked) {
                sessionManager.createLoginSession(username, "Usuario $username")
            }
            Toast.makeText(this, "Acceso concedido como $username", Toast.LENGTH_SHORT).show()
            openMainActivity()
        } else {
            Toast.makeText(this, "Credenciales incorrectas (Pruebe con admin / admin123)", Toast.LENGTH_LONG).show()
        }
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
