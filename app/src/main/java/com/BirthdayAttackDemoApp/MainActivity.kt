package com.BirthdayAttackDemoApp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ConnectionEstablisher
import Peer
import androidx.lifecycle.ViewModelProvider
import carriers.Carriers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun randomString(): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return  (1..2)
        .map { allowedChars.random() }
        .joinToString("")
}

class MainActivity : AppCompatActivity() {
    private val myUUID = randomString()
    private val myProvider = Carriers.Test
    private lateinit var ipAddressViewModel: IPAddressViewModel



    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Access the UI elements
        val editTextIpAddress: EditText = findViewById(R.id.editTextIpAddress)
        val uuidText: EditText = findViewById(R.id.editUUID)
        val spinnerProvider: Spinner = findViewById(R.id.spinnerProvider)
        val buttonConnect: Button = findViewById(R.id.buttonConnect)
        val labelStatus: TextView = findViewById(R.id.labelStatus)
        labelStatus.text = myUUID


        // Set up the spinner with options
        val providers = Carriers.entries // "Lebara", "Kpn", "LycaNL", "VodafoneNl"
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, providers)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProvider.adapter = adapter

        ipAddressViewModel = ViewModelProvider(this)[IPAddressViewModel::class.java]
        ipAddressViewModel.ipLiveData.observe(this) { ip ->
            val str = "IP Address: $ip"
            val str2 = "${labelStatus.text} \n $str"
            labelStatus.text = str2
        }

        ipAddressViewModel.retrieveIpAddress()

        // Set up the button click listener
        buttonConnect.setOnClickListener {
            val ipAddress = editTextIpAddress.text.toString()
            val uuid = uuidText.text
            val selectedProvider = Carriers.valueOf(spinnerProvider.selectedItem.toString())
            val text = "Connecting to $selectedProvider at $ipAddress..."
            labelStatus.text = text

//            for (i in 0..100) {
            val connectionEstablisher = ConnectionEstablisher()
            val peer1 = Peer(selectedProvider,uuid.toString(), ipAddress)

            GlobalScope.launch {
                val result = connectionEstablisher.start(myProvider, myUUID, listOf(peer1))
                result.forEach {
                    labelStatus.text = "$it"
                }
            }
        }
    }
}
