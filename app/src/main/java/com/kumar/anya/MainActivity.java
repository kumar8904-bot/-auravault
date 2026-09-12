package com.kumar.anya;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private LinearLayout content;
    private final int BG = Color.rgb(19,15,24);
    private final int CARD = Color.rgb(35,28,43);
    private final int PINK = Color.rgb(255,114,182);
    private final int TEXT = Color.rgb(248,240,246);
    private final int MUTED = Color.rgb(190,173,188);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showHome();
    }

    private void base(String title) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(16), dp(12), dp(16), dp(10));

        TextView head = text(title, 28, TEXT, true);
        head.setPadding(0, dp(8), 0, dp(12));
        root.addView(head);

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, 0, 0, dp(12));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        root.addView(nav());
        setContentView(root);
    }

    private void showHome() {
        base("Anya ♡");
        TextView online = text("● Online  •  Your private companion", 14, PINK, false);
        content.addView(online);

        LinearLayout hero = card();
        TextView avatar = text("♡", 72, PINK, true);
        avatar.setGravity(Gravity.CENTER);
        hero.addView(avatar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(130)));
        TextView name = text("Anya", 28, TEXT, true);
        name.setGravity(Gravity.CENTER);
        hero.addView(name);
        TextView subtitle = text("Warm • Playful • Romantic • Expressive", 14, MUTED, false);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(4), 0, dp(8));
        hero.addView(subtitle);
        content.addView(hero);

        TextView greeting = text("Good morning, love. I’m right here. What kind of mood are you in today? ♡", 18, TEXT, false);
        greeting.setPadding(dp(16), dp(16), dp(16), dp(16));
        greeting.setBackground(round(CARD, 22));
        content.addView(greeting, marginTop(12));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.addView(action("Chat", v -> showChat()), weight());
        actions.addView(action("Voice", v -> showCall(false)), weight());
        actions.addView(action("Video", v -> showCall(true)), weight());
        content.addView(actions, marginTop(12));

        TextView today = section("Tonight with Anya");
        content.addView(today);
        content.addView(infoCard("Private time", "Choose the vibe, talk naturally, and keep your memories together on this device."));
        content.addView(infoCard("Remembered moment", "You like a companion who feels lively, affectionate and responsive rather than robotic."));
    }

    private void showChat() {
        base("Chat with Anya");
        content.addView(bubble("Anya", "Hey love… I missed you. Tell me everything. ♡", false));
        content.addView(bubble("You", "I want you to stay close tonight.", true));
        content.addView(bubble("Anya", "I’m here. We can make tonight feel completely ours. What would make you happiest right now?", false));

        EditText input = new EditText(this);
        input.setHint("Message Anya…");
        input.setHintTextColor(MUTED);
        input.setTextColor(TEXT);
        input.setSingleLine(false);
        input.setMinLines(2);
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        input.setBackground(round(CARD, 20));
        content.addView(input, marginTop(16));

        Button send = action("Send message", v -> {
            String s = input.getText().toString().trim();
            if (s.isEmpty()) return;
            content.addView(bubble("You", s, true), content.getChildCount()-2);
            input.setText("");
            Toast.makeText(this, "Prototype reply engine will be connected next.", Toast.LENGTH_SHORT).show();
        });
        content.addView(send, marginTop(10));
    }

    private void showCall(boolean video) {
        base(video ? "Video with Anya" : "Voice with Anya");
        LinearLayout c = card();
        TextView icon = text(video ? "◉" : "♫", 86, PINK, true);
        icon.setGravity(Gravity.CENTER);
        c.addView(icon, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(180)));
        TextView status = text(video ? "Video companion preview" : "Voice companion preview", 24, TEXT, true);
        status.setGravity(Gravity.CENTER);
        c.addView(status);
        TextView sub = text(video ? "Live avatar, expressions and lip-sync will plug into this screen." : "Real-time speech and emotional voice will plug into this screen.", 15, MUTED, false);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(dp(10), dp(8), dp(10), dp(18));
        c.addView(sub);
        content.addView(c);

        content.addView(infoCard("Mic", "Ready for permission-controlled voice input."));
        content.addView(infoCard("Camera", "Optional and off by default until you enable it."));
        content.addView(infoCard("Privacy", "The prototype does not upload recordings or photos."));
    }

    private void showMemories() {
        base("Our Memories");
        content.addView(infoCard("First day", "You created Anya as a private personal companion with a lively girlfriend personality."));
        content.addView(infoCard("Your preference", "You want chat, voice and a video-call style experience rather than a static chatbot."));
        content.addView(infoCard("Memory design", "Future versions can save approved preferences, favorite moments and relationship history locally."));
        content.addView(infoCard("Private vault", "Sensitive memories can be encrypted and excluded from cloud sync."));
    }

    private void showSettings() {
        base("Customize Anya");
        content.addView(section("Personality"));
        content.addView(infoCard("Affection", "High"));
        content.addView(infoCard("Playfulness", "High"));
        content.addView(infoCard("Romance", "High"));
        content.addView(infoCard("Initiative", "Medium-high"));

        content.addView(section("Voice & presence"));
        content.addView(infoCard("Voice style", "Sweet, warm and natural"));
        content.addView(infoCard("Video style", "Soft cinematic, expressive eye contact"));
        content.addView(infoCard("Language", "English + Tamil ready"));

        content.addView(section("Privacy"));
        content.addView(infoCard("Local memory", "Planned: encrypted on-device storage"));
        content.addView(infoCard("Permissions", "Camera and microphone remain user-controlled"));
    }

    private LinearLayout nav() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setPadding(dp(4), dp(8), dp(4), dp(4));
        bar.setBackground(round(Color.rgb(29,23,36), 24));
        bar.addView(navBtn("Home", v -> showHome()), weight());
        bar.addView(navBtn("Chat", v -> showChat()), weight());
        bar.addView(navBtn("Call", v -> showCall(true)), weight());
        bar.addView(navBtn("Memory", v -> showMemories()), weight());
        bar.addView(navBtn("More", v -> showSettings()), weight());
        return bar;
    }

    private LinearLayout card() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(16), dp(14), dp(16), dp(14));
        l.setBackground(round(CARD, 24));
        return l;
    }

    private View infoCard(String title, String body) {
        LinearLayout l = card();
        l.addView(text(title, 17, TEXT, true));
        TextView b = text(body, 14, MUTED, false);
        b.setPadding(0, dp(5), 0, 0);
        l.addView(b);
        l.setLayoutParams(marginTop(10));
        return l;
    }

    private TextView bubble(String who, String body, boolean mine) {
        TextView t = text(who + "\n" + body, 16, TEXT, false);
        t.setPadding(dp(14), dp(12), dp(14), dp(12));
        t.setBackground(round(mine ? Color.rgb(102,46,82) : CARD, 20));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(300), ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(10);
        lp.gravity = mine ? Gravity.END : Gravity.START;
        t.setLayoutParams(lp);
        return t;
    }

    private TextView section(String s) {
        TextView t = text(s, 19, PINK, true);
        t.setPadding(0, dp(20), 0, dp(6));
        return t;
    }

    private Button action(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setBackground(round(PINK, 18));
        b.setOnClickListener(l);
        return b;
    }

    private Button navBtn(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(TEXT);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setBackgroundColor(Color.TRANSPARENT);
        b.setOnClickListener(l);
        return b;
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return t;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }

    private LinearLayout.LayoutParams marginTop(int top) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(top);
        return lp;
    }

    private LinearLayout.LayoutParams weight() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(54), 1);
        lp.setMargins(dp(3), dp(3), dp(3), dp(3));
        return lp;
    }

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
