package com.hhgf.brimo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "brimo_channel";
    private static final int NOTIF_ID = 1001;

    private LinearLayout notifContainer;
    private LinearLayout detailPanel;
    private EditText inputSender, inputAmount, inputRef, inputSaldo, inputRekening, inputKet, inputMuncul;
    private Spinner inputBank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        notifContainer = findViewById(R.id.notifContainer);
        detailPanel = findViewById(R.id.detailPanel);
        inputSender = findViewById(R.id.inputSender);
        inputAmount = findViewById(R.id.inputAmount);
        inputRef = findViewById(R.id.inputRef);
        inputSaldo = findViewById(R.id.inputSaldo);
        inputRekening = findViewById(R.id.inputRekening);
        inputKet = findViewById(R.id.inputKet);
        inputMuncul = findViewById(R.id.inputMuncul);
        inputBank = findViewById(R.id.inputBank);
        Button btnKirim = findViewById(R.id.btnKirim);

        String[] banks = {"BRI", "BCA", "Mandiri", "BNI", "BSI", "Dana", "OVO", "GoPay", "ShopeePay"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, banks);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputBank.setAdapter(adapter);

        createNotificationChannel();

        btnKirim.setOnClickListener(v -> kirimNotifikasi());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Transaksi BRImo", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifikasi transaksi masuk");
            channel.enableVibration(true);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private String formatRupiah(long n) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));
        return "Rp " + nf.format(n);
    }

    private String waktuSekarang() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private String tanggalSekarang() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
    }

    private String generateRandomKet() {
        Random rnd = new Random();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder("BFST025");
        for (int i = 0; i < 12; i++) {
            sb.append(rnd.nextInt(10));
        }
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateRandomMuncul() {
        Random rnd = new Random();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder("PIN");
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void kirimNotifikasi() {
        String sender = inputSender.getText().toString();
        long amount = parseLong(inputAmount.getText().toString());
        String bank = inputBank.getSelectedItem().toString();
        String ref = inputRef.getText().toString();
        String rekening = inputRekening.getText().toString();
        long saldo = parseLong(inputSaldo.getText().toString());
        
        String ket = inputKet.getText().toString();
        if (TextUtils.isEmpty(ket)) ket = generateRandomKet();
        
        String muncul = inputMuncul.getText().toString();
        if (TextUtils.isEmpty(muncul)) muncul = generateRandomMuncul();

        String time = waktuSekarang();
        String date = tanggalSekarang();
        String nominal = formatRupiah(amount);

        String textNotif = "Sobat BRI! Dana " + nominal + " masuk ke rekening " + rekening + 
                           " pada " + date + " " + time + " KET.:" + ket + " MUNCUL :" + muncul;

        View item = LayoutInflater.from(this).inflate(R.layout.item_notif, notifContainer, false);
        TextView itemTime = item.findViewById(R.id.itemTime);
        TextView itemText = item.findViewById(R.id.itemText);
        itemTime.setText("sekarang");
        itemText.setText(textNotif);
        notifContainer.addView(item, 0);

        detailPanel.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.detailNominal)).setText(nominal);
        ((TextView) findViewById(R.id.detailSender)).setText(sender);
        ((TextView) findViewById(R.id.detailBank)).setText(bank);
        ((TextView) findViewById(R.id.detailRef)).setText(ref);
        ((TextView) findViewById(R.id.detailTime)).setText(time + " WIB");
        ((TextView) findViewById(R.id.detailSaldo)).setText(formatRupiah(saldo));

        tampilkanSystemNotification(textNotif);
    }

    private void tampilkanSystemNotification(String pesan) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("BRImo")
                .setContentText(pesan)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(pesan))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis());

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIF_ID, builder.build());
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return 0; }
    }
}
