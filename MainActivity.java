package com.digitaltelevision.player;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.KeyEvent;
import android.widget.*;
import android.text.InputType;
import android.graphics.Typeface;
import android.content.res.ColorStateList;
import android.content.SharedPreferences;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.common.MediaItem;
import androidx.media3.ui.PlayerView;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(7,22,41), BLUE = Color.rgb(54,163,255), WHITE = Color.WHITE;
    private EditText playlistInput;
    private TextView status;
    private ListView channelList;
    private PlayerView playerView;
    private ExoPlayer player;
    private final ArrayList<String> names = new ArrayList<>();
    private final ArrayList<String> urls = new ArrayList<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private SharedPreferences preferences;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        preferences = getSharedPreferences("digitaltelevision_player", MODE_PRIVATE);
        buildUi();
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        if (!preferences.getString("playlist_url", "").isEmpty()) {
            playlistInput.post(this::loadPlaylist);
        }
    }

    private TextView text(String value, int size, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextColor(WHITE); t.setTextSize(size);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private void buildUi() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(28,18,28,18); root.setBackgroundColor(BG);
        TextView title = text("DIGITALTELEVISION  |  PLAYER", 25, true); title.setTextColor(BLUE); root.addView(title);
        TextView subtitle = text("Player demonstrativ • Android TV", 13, false); subtitle.setPadding(0,0,0,12); root.addView(subtitle);
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        playlistInput = new EditText(this); playlistInput.setSingleLine(true); playlistInput.setTextColor(WHITE); playlistInput.setHintTextColor(Color.LTGRAY); playlistInput.setHint("Introdu linkul playlistului M3U"); playlistInput.setText(preferences.getString("playlist_url", "")); playlistInput.setTextSize(16); playlistInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI); playlistInput.setSelectAllOnFocus(true);
        row.addView(playlistInput, new LinearLayout.LayoutParams(0,58,1));
        Button load = new Button(this); load.setText("ÎNCARCĂ LISTA"); load.setTextColor(WHITE); load.setBackgroundTintList(ColorStateList.valueOf(BLUE)); row.addView(load); root.addView(row);
        status = text("Introdu un link M3U pentru a încărca posturile.", 14, false); status.setPadding(0,10,0,10); root.addView(status);
        LinearLayout content = new LinearLayout(this); content.setOrientation(LinearLayout.HORIZONTAL);
        channelList = new ListView(this); channelList.setBackgroundColor(Color.rgb(13,39,66)); channelList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        content.addView(channelList, new LinearLayout.LayoutParams(0,0,0.38f));
        playerView = new PlayerView(this); playerView.setUseController(true); content.addView(playerView, new LinearLayout.LayoutParams(0,0,0.62f));
        root.addView(content, new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        load.setOnClickListener(v -> loadPlaylist());
        channelList.setOnItemClickListener((parent, view, position, id) -> play(position));
        playlistInput.setOnEditorActionListener((v, actionId, event) -> { loadPlaylist(); return true; });
    }
    private void loadPlaylist() {
        final String address = playlistInput.getText().toString().trim();
        if (!address.startsWith("http://") && !address.startsWith("https://")) { status.setText("Introdu un link M3U valid, care începe cu http:// sau https://."); return; }
        preferences.edit().putString("playlist_url", address).apply();
        status.setText("Se încarcă playlistul...");
        executor.execute(() -> {
            ArrayList<String> newNames = new ArrayList<>(), newUrls = new ArrayList<>();
            try {
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new URL(address).openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);
                connection.setRequestProperty("User-Agent", "DigitalTelevisionPlayer/0.2");
                connection.setInstanceFollowRedirects(true);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                String line, pending = "Canal";
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("#EXTINF")) { int comma = line.lastIndexOf(','); pending = comma >= 0 ? line.substring(comma+1).trim() : "Canal"; }
                    else if (!line.isEmpty() && !line.startsWith("#")) {
                        try { newUrls.add(new URL(new URL(address), line).toString()); }
                        catch (Exception ignored) { newUrls.add(line); }
                        newNames.add(pending); pending = "Canal";
                    }
                }
                runOnUiThread(() -> {
                    names.clear(); names.addAll(newNames); urls.clear(); urls.addAll(newUrls);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names) {
                        @Override public View getView(int position, View convert, android.view.ViewGroup parent) {
                            TextView tv = (TextView) super.getView(position, convert, parent); tv.setTextColor(WHITE); tv.setTextSize(16); tv.setPadding(16,12,12,12); return tv;
                        }
                    };
                    channelList.setAdapter(adapter); status.setText("Listă încărcată: " + names.size() + " canale. Selectează un canal.");
                    if (!names.isEmpty()) channelList.requestFocus();
                });
                } finally { connection.disconnect(); }
            } catch (Exception e) { runOnUiThread(() -> status.setText("Nu am putut încărca lista. Verifică linkul M3U și conexiunea la internet.")); }
        });
    }
    private void play(int index) {
        if (index < 0 || index >= urls.size()) return;
        try { player.setMediaItem(MediaItem.fromUri(urls.get(index))); player.prepare(); player.play(); status.setText("Se redă: " + names.get(index)); }
        catch (Exception e) { status.setText("Nu pot reda acest canal."); }
    }
    @Override public void onDestroy() { super.onDestroy(); if (player != null) player.release(); executor.shutdownNow(); }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) { return super.onKeyDown(keyCode, event); }
}
