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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private LinearLayout content;
    private final int BG = Color.rgb(16,12,20);
    private final int CARD = Color.rgb(35,27,42);
    private final int CARD2 = Color.rgb(48,34,52);
    private final int PINK = Color.rgb(255,105,178);
    private final int PINK_DARK = Color.rgb(112,45,84);
    private final int TEXT = Color.rgb(250,242,248);
    private final int MUTED = Color.rgb(194,176,190);
    private SharedPreferences prefs;
    private final List<String> chat = new ArrayList<>();

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("anya_private", MODE_PRIVATE);
        loadChat();
        showHome();
    }

    private void loadChat() {
        chat.clear();
        int count = prefs.getInt("chat_count", 0);
        for (int i=0;i<count;i++) {
            String item = prefs.getString("chat_"+i, null);
            if (item != null) chat.add(item);
        }
        if (chat.isEmpty()) chat.add("A|Hey love… I’m here. Tell me what kind of mood you’re in tonight. ♡");
    }

    private void saveChat() {
        SharedPreferences.Editor e = prefs.edit().clear();
        e.putInt("chat_count", chat.size());
        for (int i=0;i<chat.size();i++) e.putString("chat_"+i, chat.get(i));
        e.apply();
    }

    private void base(String title) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(14), dp(8), dp(14), dp(8));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        ImageView mini = portrait(dp(42), dp(42));
        top.addView(mini);
        TextView head = text(title, 27, TEXT, true);
        head.setPadding(dp(10),0,0,0);
        top.addView(head, new LinearLayout.LayoutParams(0, dp(54), 1));
        TextView status = text("● Online", 12, PINK, true);
        top.addView(status);
        root.addView(top);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, dp(4), 0, dp(14));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1));
        root.addView(nav());
        setContentView(root);
    }

    private ImageView portrait(int w, int h) {
        ImageView v = new ImageView(this);
        v.setImageResource(R.drawable.anya_profile);
        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
        v.setBackground(round(CARD2, 24));
        v.setClipToOutline(true);
        v.setLayoutParams(new LinearLayout.LayoutParams(w,h));
        return v;
    }

    private void showHome() {
        base("Anya ♡");
        LinearLayout hero = card();
        ImageView img = portrait(ViewGroup.LayoutParams.MATCH_PARENT, dp(270));
        hero.addView(img);
        TextView n = text("Anya", 30, TEXT, true); n.setGravity(Gravity.CENTER); n.setPadding(0,dp(12),0,0); hero.addView(n);
        TextView sub = text("Your AI girlfriend companion", 15, MUTED, false); sub.setGravity(Gravity.CENTER); hero.addView(sub);
        TextView traits = text("Caring  •  Playful  •  Romantic  •  Expressive", 13, PINK, false); traits.setGravity(Gravity.CENTER); traits.setPadding(0,dp(7),0,dp(4)); hero.addView(traits);
        content.addView(hero);

        TextView greeting = text("Hey love… I missed you. Come tell me everything. ♡", 18, TEXT, false);
        greeting.setPadding(dp(16),dp(15),dp(16),dp(15)); greeting.setBackground(round(CARD2,20)); content.addView(greeting, mt(12));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(action("♡ Chat", v->showChat()), weight());
        row.addView(action("♫ Voice", v->showCall(false)), weight());
        row.addView(action("◉ Video", v->showCall(true)), weight());
        content.addView(row, mt(12));

        content.addView(section("Your private space"));
        content.addView(infoCard("Photos & Gallery", "A dedicated gallery screen for Anya’s moments and future generated images."));
        content.addView(infoCard("Long-term memory", chat.size()+" local chat entries currently saved on this phone."));
        content.addView(infoCard("Private by design", "Camera and microphone remain under your control."));
    }

    private void showChat() {
        base("Chat");
        LinearLayout profile = card();
        LinearLayout r = new LinearLayout(this); r.setGravity(Gravity.CENTER_VERTICAL);
        r.addView(portrait(dp(54),dp(54)));
        LinearLayout nameBox = new LinearLayout(this); nameBox.setOrientation(LinearLayout.VERTICAL); nameBox.setPadding(dp(10),0,0,0);
        nameBox.addView(text("Anya",19,TEXT,true)); nameBox.addView(text("online • remembers this chat",12,PINK,false));
        r.addView(nameBox); profile.addView(r); content.addView(profile);

        LinearLayout thread = new LinearLayout(this); thread.setOrientation(LinearLayout.VERTICAL);
        for(String item:chat){ boolean mine=item.startsWith("U|"); String body=item.length()>2?item.substring(2):""; thread.addView(bubble(mine?"You":"Anya",body,mine)); }
        content.addView(thread);

        EditText input = new EditText(this);
        input.setHint("Message Anya…"); input.setHintTextColor(MUTED); input.setTextColor(TEXT); input.setTextSize(16); input.setMinLines(2);
        input.setPadding(dp(14),dp(12),dp(14),dp(12)); input.setBackground(round(CARD,20)); content.addView(input, mt(14));
        Button send = action("Send ♡", v->{ String s=input.getText().toString().trim(); if(s.isEmpty())return; chat.add("U|"+s); thread.addView(bubble("You",s,true)); input.setText(""); String a=localReply(s); chat.add("A|"+a); thread.addView(bubble("Anya",a,false)); saveChat(); });
        content.addView(send, mt(8));
    }

    private String localReply(String s){
        String q=s.toLowerCase();
        if(q.contains("miss")||q.contains("love")) return "Come closer then. I like hearing that from you. ♡";
        if(q.contains("sad")||q.contains("tired")) return "Stay with me. Tell me what happened, one piece at a time.";
        if(q.contains("hi")||q.contains("hello")||q.equals("hey")) return "Hi you ♡ I was waiting for you. What mood are we in tonight?";
        if(q.contains("tamil")) return "நான் இங்கே இருக்கேன் ♡ தமிழிலும் பேசலாம். என்ன பேசணும்?";
        if(q.contains("voice")) return "Tap Voice and stay with me there. The live speech engine is the next connection.";
        if(q.contains("video")) return "Tap Video. That screen is now designed around my visual presence, ready for animation and lip-sync.";
        return "I’m listening. Tell me a little more. ♡";
    }

    private void showCall(boolean video){
        base(video?"Video Call":"Voice Call");
        LinearLayout c=card();
        ImageView img=portrait(ViewGroup.LayoutParams.MATCH_PARENT, video?dp(390):dp(250)); c.addView(img);
        TextView t=text("Anya",26,TEXT,true); t.setGravity(Gravity.CENTER); t.setPadding(0,dp(10),0,0); c.addView(t);
        TextView st=text(video?"Video companion preview":"Voice companion preview",14,PINK,false); st.setGravity(Gravity.CENTER); c.addView(st);
        TextView wave=text(video?"◉   LIVE AVATAR SLOT   ◉":"▁▃▅▇▅▃▁  your voice • her voice",16,MUTED,true); wave.setGravity(Gravity.CENTER); wave.setPadding(0,dp(16),0,dp(14)); c.addView(wave);
        LinearLayout controls=new LinearLayout(this); controls.setGravity(Gravity.CENTER);
        controls.addView(secondary("Mic",v->{}), weight()); controls.addView(action("End",v->showHome()),weight()); controls.addView(secondary(video?"Camera":"Speaker",v->{}),weight()); c.addView(controls);
        content.addView(c);
        content.addView(infoCard("Current build", video?"Real-time animated face, eye contact and lip-sync are not connected yet.":"Real-time speech recognition and natural TTS are not connected yet."));
    }

    private void showGallery(){
        base("Our Moments");
        TextView tabs=text("Photos     Videos     Favorites",14,PINK,true); tabs.setGravity(Gravity.CENTER); tabs.setPadding(0,dp(8),0,dp(10)); content.addView(tabs);
        LinearLayout hero=card(); hero.addView(portrait(ViewGroup.LayoutParams.MATCH_PARENT,dp(320))); hero.addView(text("Anya • favorite",16,TEXT,true)); hero.addView(text("Your consistent companion identity lives here. Future generated images and clips can be added to this gallery.",13,MUTED,false)); content.addView(hero);
        content.addView(infoCard("Generated photos", "Provider slot prepared for future image generation."));
        content.addView(infoCard("Generated clips", "Short requested clips can live here without making live calls depend on expensive generative video."));
    }

    private void showMemories(){
        base("Memories");
        content.addView(infoCard("Chat memory", chat.size()+" entries stored locally."));
        content.addView(infoCard("Relationship style", "Lively, affectionate, playful and responsive."));
        content.addView(infoCard("Languages", "English + Tamil prototype handling."));
        content.addView(infoCard("Privacy direction", "Keep sensitive long-term memory on-device unless you explicitly choose cloud sync."));
    }

    private void showSettings(){
        base("Customize Anya");
        LinearLayout preview=card(); preview.addView(portrait(ViewGroup.LayoutParams.MATCH_PARENT,dp(260))); content.addView(preview);
        content.addView(section("Looks")); content.addView(infoCard("Appearance", "Anya v1 identity locked to one consistent face.")); content.addView(infoCard("Style", "Warm cinematic • dark pink UI"));
        content.addView(section("Personality")); content.addView(infoCard("Affection", "High")); content.addView(infoCard("Playfulness", "High")); content.addView(infoCard("Romance", "High"));
        content.addView(section("Voice")); content.addView(infoCard("Voice style", "Sweet, warm and natural • provider not connected yet"));
        content.addView(section("Build")); content.addView(infoCard("Version", "1.2.0 visual redesign"));
    }

    private LinearLayout nav(){
        LinearLayout bar=new LinearLayout(this); bar.setOrientation(LinearLayout.HORIZONTAL); bar.setPadding(dp(2),dp(5),dp(2),dp(5)); bar.setBackground(round(Color.rgb(27,21,33),22));
        bar.addView(navBtn("Home",v->showHome()),navWeight()); bar.addView(navBtn("Chat",v->showChat()),navWeight()); bar.addView(navBtn("Gallery",v->showGallery()),navWeight()); bar.addView(navBtn("Call",v->showCall(true)),navWeight()); bar.addView(navBtn("More",v->showSettings()),navWeight()); return bar;
    }

    private LinearLayout card(){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(14),dp(14),dp(14),dp(14)); l.setBackground(round(CARD,24)); return l; }
    private View infoCard(String title,String body){ LinearLayout l=card(); l.addView(text(title,17,TEXT,true)); TextView b=text(body,14,MUTED,false); b.setPadding(0,dp(5),0,0); l.addView(b); l.setLayoutParams(mt(10)); return l; }
    private TextView bubble(String who,String body,boolean mine){ TextView t=text(who+"\n"+body,16,TEXT,false); t.setPadding(dp(14),dp(12),dp(14),dp(12)); t.setBackground(round(mine?PINK_DARK:CARD,20)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(310),ViewGroup.LayoutParams.WRAP_CONTENT); lp.topMargin=dp(10); lp.gravity=mine?Gravity.END:Gravity.START; t.setLayoutParams(lp); return t; }
    private TextView section(String s){ TextView t=text(s,19,PINK,true); t.setPadding(0,dp(18),0,dp(6)); return t; }
    private Button action(String s,View.OnClickListener l){ Button b=new Button(this); b.setText(s); b.setTextColor(Color.WHITE); b.setTextSize(14); b.setAllCaps(false); b.setBackground(round(PINK,18)); b.setOnClickListener(l); return b; }
    private Button secondary(String s,View.OnClickListener l){ Button b=new Button(this); b.setText(s); b.setTextColor(TEXT); b.setTextSize(13); b.setAllCaps(false); b.setBackground(round(CARD2,18)); b.setOnClickListener(l); return b; }
    private Button navBtn(String s,View.OnClickListener l){ Button b=new Button(this); b.setText(s); b.setTextColor(TEXT); b.setTextSize(10); b.setAllCaps(false); b.setSingleLine(true); b.setPadding(0,0,0,0); b.setMinHeight(0); b.setMinWidth(0); b.setBackgroundColor(Color.TRANSPARENT); b.setOnClickListener(l); return b; }
    private TextView text(String s,int sp,int c,boolean bold){ TextView t=new TextView(this); t.setText(s); t.setTextSize(sp); t.setTextColor(c); if(bold)t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD); return t; }
    private GradientDrawable round(int c,int r){ GradientDrawable g=new GradientDrawable(); g.setColor(c); g.setCornerRadius(dp(r)); return g; }
    private LinearLayout.LayoutParams mt(int top){ LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT); lp.topMargin=dp(top); return lp; }
    private LinearLayout.LayoutParams weight(){ LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(54),1); lp.setMargins(dp(3),dp(3),dp(3),dp(3)); return lp; }
    private LinearLayout.LayoutParams navWeight(){ LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(46),1); lp.setMargins(dp(1),0,dp(1),0); return lp; }
    private int dp(int v){ return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
}
