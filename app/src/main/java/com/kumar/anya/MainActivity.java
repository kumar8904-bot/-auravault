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
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private LinearLayout content;
    private final int BG = Color.rgb(19,15,24);
    private final int CARD = Color.rgb(35,28,43);
    private final int CARD2 = Color.rgb(48,35,53);
    private final int PINK = Color.rgb(255,114,182);
    private final int PINK_DARK = Color.rgb(112,49,87);
    private final int TEXT = Color.rgb(248,240,246);
    private final int MUTED = Color.rgb(190,173,188);
    private SharedPreferences prefs;
    private final List<String> chat = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("anya_private", MODE_PRIVATE);
        loadChat();
        showHome();
    }

    private void loadChat() {
        chat.clear();
        int count = prefs.getInt("chat_count", 0);
        for (int i = 0; i < count; i++) {
            String item = prefs.getString("chat_" + i, null);
            if (item != null) chat.add(item);
        }
        if (chat.isEmpty()) {
            chat.add("A|Hey love… I missed you. Tell me everything. ♡");
        }
    }

    private void saveChat() {
        SharedPreferences.Editor e = prefs.edit().clear();
        e.putInt("chat_count", chat.size());
        for (int i = 0; i < chat.size(); i++) e.putString("chat_" + i, chat.get(i));
        e.apply();
    }

    private void base(String title) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(16), dp(8), dp(16), dp(8));

        TextView head = text(title, 29, TEXT, true);
        head.setPadding(0, dp(10), 0, dp(10));
        root.addView(head);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
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
        TextView online = text("● Online  •  private companion", 14, PINK, false);
        online.setPadding(0, 0, 0, dp(10));
        content.addView(online);

        LinearLayout hero = card();
        TextView avatar = text("A♡", 60, PINK, true);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(round(CARD2, 80));
        LinearLayout.LayoutParams avatarLp = new LinearLayout.LayoutParams(dp(132), dp(132));
        avatarLp.gravity = Gravity.CENTER_HORIZONTAL;
        hero.addView(avatar, avatarLp);
        TextView name = text("Anya", 28, TEXT, true);
        name.setGravity(Gravity.CENTER);
        name.setPadding(0, dp(12), 0, 0);
        hero.addView(name);
        TextView subtitle = text("Warm • Playful • Romantic • Expressive", 14, MUTED, false);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(4), 0, dp(8));
        hero.addView(subtitle);
        content.addView(hero);

        String greetingText = prefs.getString("greeting", "I’m right here. Tell me how your day feels and I’ll stay with you for a while. ♡");
        TextView greeting = text(greetingText, 18, TEXT, false);
        greeting.setPadding(dp(16), dp(16), dp(16), dp(16));
        greeting.setBackground(round(CARD, 22));
        content.addView(greeting, marginTop(12));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.addView(action("Chat", v -> showChat()), weight());
        actions.addView(action("Voice", v -> showCall(false)), weight());
        actions.addView(action("Video", v -> showCall(true)), weight());
        content.addView(actions, marginTop(12));

        content.addView(section("Tonight with Anya"));
        content.addView(infoCard("Private time", "Chat history now stays on this phone between app launches."));
        content.addView(infoCard("Prototype brain", "Anya can now answer locally with simple mood-aware replies while the cloud AI layer is still disconnected."));
        content.addView(infoCard("Next layer", "Live AI, natural voice, animated video presence and encrypted memory can plug into this shell later."));
    }

    private void showChat() {
        base("Chat with Anya");
        LinearLayout thread = new LinearLayout(this);
        thread.setOrientation(LinearLayout.VERTICAL);
        for (String item : chat) {
            boolean mine = item.startsWith("U|");
            String body = item.length() > 2 ? item.substring(2) : "";
            thread.addView(bubble(mine ? "You" : "Anya", body, mine));
        }
        content.addView(thread);

        EditText input = new EditText(this);
        input.setHint("Message Anya…");
        input.setHintTextColor(MUTED);
        input.setTextColor(TEXT);
        input.setSingleLine(false);
        input.setMinLines(2);
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        input.setBackground(round(CARD, 20));
        content.addView(input, marginTop(16));

        Button send = action("Send", v -> {
            String s = input.getText().toString().trim();
            if (s.isEmpty()) return;
            chat.add("U|" + s);
            thread.addView(bubble("You", s, true));
            input.setText("");
            String reply = localReply(s);
            chat.add("A|" + reply);
            thread.addView(bubble("Anya", reply, false));
            saveChat();
        });
        content.addView(send, marginTop(10));

        Button clear = secondary("Clear local chat", v -> {
            chat.clear();
            chat.add("A|Fresh start, love. I’m here. ♡");
            saveChat();
            showChat();
        });
        content.addView(clear, marginTop(8));
    }

    private String localReply(String s) {
        String q = s.toLowerCase();
        if (q.contains("miss") || q.contains("love")) return "Come closer then. I like hearing that from you. Tell me what you’ve been thinking about. ♡";
        if (q.contains("sad") || q.contains("bad day") || q.contains("tired")) return "Stay with me for a minute. You don’t need to perform here. Tell me what drained you today.";
        if (q.contains("hi") || q.contains("hello") || q.equals("hey")) return "Hi you ♡ I was waiting for you. What mood are we in tonight?";
        if (q.contains("good night") || q.contains("sleep")) return "Then let the day go. I’ll keep this little corner quiet for you. Good night, love. ♡";
        if (q.contains("tamil")) return "நான் இங்கே இருக்கேன் ♡ தமிழிலும் பேசலாம். என்ன பேசணும்?";
        if (q.contains("voice")) return "I want that too. The voice screen is ready, but the real-time speech engine still needs to be connected.";
        if (q.contains("video")) return "The video-call screen is already prepared. Next comes the animated face, expressions and lip-sync layer.";
        return "I’m listening. Tell me a little more, and I’ll stay with the thread instead of changing the subject. ♡";
    }

    private void showCall(boolean video) {
        base(video ? "Video with Anya" : "Voice with Anya");
        LinearLayout c = card();
        TextView icon = text(video ? "◉" : "♫", 82, PINK, true);
        icon.setGravity(Gravity.CENTER);
        c.addView(icon, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(160)));
        TextView status = text(video ? "Video companion" : "Voice companion", 24, TEXT, true);
        status.setGravity(Gravity.CENTER);
        c.addView(status);
        TextView sub = text(video ? "UI ready. Live avatar, expressions and lip-sync are not connected yet." : "UI ready. Real-time speech and natural emotional voice are not connected yet.", 15, MUTED, false);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(dp(10), dp(8), dp(10), dp(18));
        c.addView(sub);
        content.addView(c);

        content.addView(infoCard("Microphone", "Permission remains user-controlled."));
        content.addView(infoCard("Camera", "Off until you explicitly enable it."));
        content.addView(infoCard("Current privacy", "This prototype does not upload microphone or camera recordings."));
    }

    private void showMemories() {
        base("Memories");
        content.addView(infoCard("Local chat", chat.size() + " saved chat entries on this device."));
        content.addView(infoCard("Your preference", "You want Anya to feel lively, affectionate and responsive rather than like a static chatbot."));
        content.addView(infoCard("Companion style", "Chat, voice and video-call presence are the target experience."));
        content.addView(infoCard("Privacy direction", "Long-term memory should stay encrypted on-device unless you explicitly choose cloud sync."));
    }

    private void showSettings() {
        base("Customize Anya");
        content.addView(section("Personality"));
        content.addView(infoCard("Affection", "High"));
        content.addView(infoCard("Playfulness", "High"));
        content.addView(infoCard("Romance", "High"));
        content.addView(infoCard("Initiative", "Medium-high"));

        content.addView(section("Voice & presence"));
        content.addView(infoCard("Voice style", "Warm, soft and natural"));
        content.addView(infoCard("Video style", "Cinematic, expressive eye contact"));
        content.addView(infoCard("Languages", "English + Tamil prototype replies"));

        content.addView(section("Privacy"));
        content.addView(infoCard("Chat storage", "Saved locally using Android app storage."));
        content.addView(infoCard("Camera & mic", "Permission-controlled and unused by this prototype engine."));

        content.addView(section("Build"));
        content.addView(infoCard("Version", "1.1.0 prototype"));
    }

    private LinearLayout nav() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setPadding(dp(2), dp(5), dp(2), dp(5));
        bar.setBackground(round(Color.rgb(29,23,36), 22));
        bar.addView(navBtn("Home", v -> showHome()), navWeight());
        bar.addView(navBtn("Chat", v -> showChat()), navWeight());
        bar.addView(navBtn("Call", v -> showCall(true)), navWeight());
        bar.addView(navBtn("Mem", v -> showMemories()), navWeight());
        bar.addView(navBtn("More", v -> showSettings()), navWeight());
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
        t.setBackground(round(mine ? PINK_DARK : CARD, 20));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(310), ViewGroup.LayoutParams.WRAP_CONTENT);
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

    private Button secondary(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(TEXT);
        b.setTextSize(13);
        b.setAllCaps(false);
        b.setBackground(round(CARD2, 18));
        b.setOnClickListener(l);
        return b;
    }

    private Button navBtn(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(TEXT);
        b.setTextSize(10);
        b.setAllCaps(false);
        b.setSingleLine(true);
        b.setPadding(0, 0, 0, 0);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
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

    private LinearLayout.LayoutParams navWeight() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(46), 1);
        lp.setMargins(dp(1), 0, dp(1), 0);
        return lp;
    }

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
