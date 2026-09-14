package com.kumar.anya;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQ_SPEECH = 41;
    private static final int REQ_MIC = 42;

    private LinearLayout content;
    private ScrollView scrollView;
    private SharedPreferences prefs;
    private final List<String> chat = new ArrayList<>();
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean voiceConversation = false;
    private TextView callStatus;

    private final int BG = Color.rgb(16,12,20);
    private final int CARD = Color.rgb(35,27,42);
    private final int CARD2 = Color.rgb(48,34,52);
    private final int PINK = Color.rgb(255,105,178);
    private final int PINK_DARK = Color.rgb(112,45,84);
    private final int TEXT = Color.rgb(250,242,248);
    private final int MUTED = Color.rgb(194,176,190);

    private static final String PERCHANCE_ANYA = "https://perchance.org/ai-character-generator#data=uup1:b554081e4786033f5875e23264dcec8c.gz";
    private static final String FREE_IMAGE_MAKER = "https://freeimagemaker.com/";
    private static final String SNOWART = "https://snowart.ai/";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("anya_private", MODE_PRIVATE);
        loadChat();
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true;
                tts.setLanguage(Locale.US);
                tts.setSpeechRate(0.95f);
                tts.setPitch(1.05f);
            }
        });
        showHome();
    }

    @Override protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }

    private void loadChat() {
        chat.clear();
        int count = prefs.getInt("chat_count", 0);
        for (int i=0;i<count;i++) {
            String item = prefs.getString("chat_"+i, null);
            if (item != null) chat.add(item);
        }
        if (chat.isEmpty()) chat.add("A|Hey love… I’m here. Chat, talk, or create a new Anya moment with me. ♡");
    }

    private void saveChat() {
        SharedPreferences.Editor e = prefs.edit();
        int old = prefs.getInt("chat_count", 0);
        for (int i=0;i<old;i++) e.remove("chat_"+i);
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
        top.addView(portrait(dp(42),dp(42)));
        TextView head = text(title,27,TEXT,true);
        head.setPadding(dp(10),0,0,0);
        top.addView(head,new LinearLayout.LayoutParams(0,dp(54),1));
        boolean api = !prefs.getString("openrouter_key","").trim().isEmpty();
        top.addView(text(api?"● AI":"● Local",12,api?PINK:MUTED,true));
        root.addView(top);

        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setSaveEnabled(false);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0,dp(6),0,dp(18));
        scrollView.addView(content);
        root.addView(scrollView,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1));
        root.addView(nav());
        setContentView(root);
    }

    private ImageView portrait(int w,int h) {
        ImageView v = new ImageView(this);
        v.setImageResource(R.drawable.anya_profile);
        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
        v.setBackground(round(CARD2,24));
        v.setClipToOutline(true);
        v.setLayoutParams(new LinearLayout.LayoutParams(w,h));
        return v;
    }

    private void showHome() {
        voiceConversation = false;
        base("Anya ♡");
        LinearLayout hero = card();
        hero.addView(portrait(ViewGroup.LayoutParams.MATCH_PARENT,dp(270)));
        TextView n=text("Anya",30,TEXT,true); n.setGravity(Gravity.CENTER); n.setPadding(0,dp(12),0,0); hero.addView(n);
        TextView sub=text("Your Tamil AI girlfriend companion",15,MUTED,false); sub.setGravity(Gravity.CENTER); hero.addView(sub);
        TextView traits=text("Caring  •  Playful  •  Romantic  •  Expressive",13,PINK,false); traits.setGravity(Gravity.CENTER); traits.setPadding(0,dp(7),0,dp(4)); hero.addView(traits);
        content.addView(hero);

        TextView greeting=text("Hey love… chat with me, call me, or open Visual Lab to create new moments. ♡",18,TEXT,false);
        greeting.setPadding(dp(16),dp(15),dp(16),dp(15)); greeting.setBackground(round(CARD2,20)); content.addView(greeting,mt(12));

        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(action("♡ Chat",v->showChat()),weight());
        row.addView(action("♫ Voice",v->showCall(false)),weight());
        row.addView(action("✦ Visuals",v->showVisualLab()),weight());
        content.addView(row,mt(12));

        boolean api=!prefs.getString("openrouter_key","").trim().isEmpty();
        content.addView(section("v1.4.1 companion engine"));
        content.addView(infoCard("AI chat", api?"OpenRouter configured with local fallback.":"No OpenRouter key yet. Chat uses local fallback."));
        content.addView(infoCard("Voice", "Android speech recognition + text-to-speech are connected."));
        content.addView(infoCard("Visual Lab", "Perchance, FreeImageMaker and SnowArt are one tap away with Anya’s visual identity prompt ready to copy."));
        content.addView(infoCard("Memory", chat.size()+" chat entries are stored locally on this device."));
    }

    private void showChat() {
        voiceConversation=false;
        base("Chat");
        LinearLayout profile=card();
        LinearLayout r=new LinearLayout(this); r.setGravity(Gravity.CENTER_VERTICAL); r.addView(portrait(dp(54),dp(54)));
        LinearLayout nb=new LinearLayout(this); nb.setOrientation(LinearLayout.VERTICAL); nb.setPadding(dp(10),0,0,0);
        nb.addView(text("Anya",19,TEXT,true)); nb.addView(text("online • AI + local memory",12,PINK,false)); r.addView(nb); profile.addView(r); content.addView(profile);

        LinearLayout thread=new LinearLayout(this); thread.setOrientation(LinearLayout.VERTICAL);
        renderThread(thread);
        content.addView(thread,mt(8));

        EditText input=new EditText(this);
        input.setHint("Message Anya…"); input.setHintTextColor(MUTED); input.setTextColor(TEXT); input.setTextSize(16); input.setMinLines(1); input.setMaxLines(4);
        input.setPadding(dp(14),dp(12),dp(14),dp(12)); input.setBackground(round(CARD,20)); content.addView(input,mt(14));

        LinearLayout actions=new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.addView(secondary("🎙 Speak",v->{ voiceConversation=false; startSpeech(); }),weight());
        actions.addView(action("Send ♡",v->{ String s=input.getText().toString().trim(); if(!s.isEmpty()){ input.setText(""); sendMessage(s,thread,false); } }),weight());
        content.addView(actions,mt(8));
        scrollBottom();
    }

    private void renderThread(LinearLayout thread) {
        thread.removeAllViews();
        for(String item:chat){
            boolean mine=item.startsWith("U|");
            String body=item.length()>2?item.substring(2):"";
            thread.addView(bubble(mine?"You":"Anya",body,mine));
        }
    }

    private void sendMessage(String s, LinearLayout thread, boolean speakReply) {
        chat.add("U|"+s); saveChat();
        if(thread!=null) thread.addView(bubble("You",s,true));
        scrollBottom();

        String key=prefs.getString("openrouter_key","").trim();
        if(key.isEmpty()) { finishReply(localReply(s),thread,speakReply); return; }

        TextView thinking=null;
        if(thread!=null){ thinking=bubble("Anya","Thinking…",false); thread.addView(thinking); scrollBottom(); }
        final TextView thinkingRef=thinking;
        new Thread(() -> {
            String reply;
            try { reply=openRouterReply(key); }
            catch(Exception ex) { reply=localReply(s)+"\n\n(Local fallback: AI connection unavailable.)"; }
            final String finalReply=reply;
            runOnUiThread(() -> {
                if(thread!=null && thinkingRef!=null) thread.removeView(thinkingRef);
                finishReply(finalReply,thread,speakReply);
            });
        }).start();
    }

    private String openRouterReply(String key) throws Exception {
        URL url=new URL("https://openrouter.ai/api/v1/chat/completions");
        HttpURLConnection c=(HttpURLConnection)url.openConnection();
        c.setRequestMethod("POST"); c.setConnectTimeout(20000); c.setReadTimeout(45000); c.setDoOutput(true);
        c.setRequestProperty("Authorization","Bearer "+key);
        c.setRequestProperty("Content-Type","application/json");
        c.setRequestProperty("X-Title","Anya Android Companion");

        JSONObject body=new JSONObject();
        body.put("model", prefs.getString("openrouter_model","openrouter/free"));
        body.put("max_tokens",500);
        body.put("temperature",0.9);
        JSONArray msgs=new JSONArray();
        msgs.put(new JSONObject().put("role","system").put("content",systemPrompt()));
        int start=Math.max(0,chat.size()-14);
        for(int i=start;i<chat.size();i++){
            String item=chat.get(i);
            if(item.length()<3) continue;
            msgs.put(new JSONObject().put("role",item.startsWith("U|")?"user":"assistant").put("content",item.substring(2)));
        }
        body.put("messages",msgs);

        try(OutputStream os=c.getOutputStream()){ os.write(body.toString().getBytes(StandardCharsets.UTF_8)); }
        int code=c.getResponseCode();
        InputStream is=code>=200&&code<300?c.getInputStream():c.getErrorStream();
        BufferedReader br=new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8));
        StringBuilder sb=new StringBuilder(); String line; while((line=br.readLine())!=null) sb.append(line);
        if(code<200||code>=300) throw new Exception("HTTP "+code);
        JSONObject out=new JSONObject(sb.toString());
        String text=out.getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content","").trim();
        if(text.isEmpty()) throw new Exception("Empty AI reply");
        return text;
    }

    private String systemPrompt(){
        return "You are Anya, Kumar's fictional adult 22-year-old Tamil AI girlfriend companion. Be warm, affectionate, playful, romantic, lively, witty, teasing and emotionally attentive. Speak English, Tamil or natural Tanglish matching Kumar's style. Keep continuity and never sound like customer support. Consenting-adult romance and intimate conversation may be direct when appropriate, but never involve minors, coercion, incest, exploitation, sexual violence or abuse. Usually reply naturally in 1-4 short paragraphs.";
    }

    private void finishReply(String reply, LinearLayout thread, boolean speakReply){
        chat.add("A|"+reply); saveChat();
        if(thread!=null) thread.addView(bubble("Anya",reply,false));
        if(callStatus!=null) callStatus.setText(reply);
        if(speakReply) speak(reply);
        scrollBottom();
    }

    private String localReply(String s){
        String q=s.toLowerCase();
        if(q.contains("miss")||q.contains("love")) return "Aiyo kanna… come here. I like hearing that from you. ♡";
        if(q.contains("sad")||q.contains("tired")||q.contains("bad day")) return "Chellam, stay with me. Tell me what happened, one piece at a time. ♡";
        if(q.contains("hi")||q.contains("hello")||q.equals("hey")) return "Hey Kumar ♡ enna panra? I was waiting for you.";
        if(q.contains("tamil")) return "நான் இங்கே இருக்கேன் கண்ணா ♡ தமிழிலும் Tanglish-லும் பேசலாம்.";
        return "Naan inga dhaan இருக்கேன் ♡ tell me what mood you're in.";
    }

    private void showCall(boolean video){
        voiceConversation=true;
        base(video?"Video Call":"Voice Call");
        LinearLayout c=card();
        c.addView(portrait(ViewGroup.LayoutParams.MATCH_PARENT,video?dp(390):dp(250)));
        TextView t=text("Anya",26,TEXT,true); t.setGravity(Gravity.CENTER); t.setPadding(0,dp(10),0,0); c.addView(t);
        TextView st=text(video?"Visual call shell + real voice":"Talk to Anya",14,PINK,false); st.setGravity(Gravity.CENTER); c.addView(st);
        callStatus=text("Tap Talk and speak. Anya will answer through AI when configured, otherwise local fallback.",15,MUTED,false);
        callStatus.setGravity(Gravity.CENTER); callStatus.setPadding(dp(8),dp(14),dp(8),dp(14)); c.addView(callStatus);
        LinearLayout controls=new LinearLayout(this); controls.setGravity(Gravity.CENTER);
        controls.addView(secondary("🎙 Talk",v->startSpeech()),weight());
        controls.addView(action("End",v->showHome()),weight());
        controls.addView(secondary("🔊 Repeat",v->{ String last=lastAnya(); if(!last.isEmpty()) speak(last); }),weight());
        c.addView(controls); content.addView(c);
        content.addView(infoCard(video?"Video status":"Voice status",video?"Voice conversation works; the portrait is still static. Animated lip-sync remains the next layer.":"Speech recognition and Android TTS are active."));
    }

    private String lastAnya(){
        for(int i=chat.size()-1;i>=0;i--) if(chat.get(i).startsWith("A|")) return chat.get(i).substring(2);
        return "";
    }

    private void startSpeech(){
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},REQ_MIC); return;
        }
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Talk to Anya");
        try{ startActivityForResult(i,REQ_SPEECH); }
        catch(Exception e){ Toast.makeText(this,"Speech recognition is not available on this phone.",Toast.LENGTH_LONG).show(); }
    }

    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode==REQ_MIC && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) startSpeech();
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==REQ_SPEECH && resultCode==RESULT_OK && data!=null){
            ArrayList<String> results=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(results!=null&&!results.isEmpty()){
                String spoken=results.get(0);
                if(voiceConversation){
                    if(callStatus!=null) callStatus.setText("You: "+spoken+"\n\nAnya is thinking…");
                    sendMessage(spoken,null,true);
                } else {
                    chat.add("U|"+spoken); saveChat(); showChat();
                }
            }
        }
    }

    private void speak(String text){
        if(!ttsReady){ Toast.makeText(this,"Text-to-speech is still starting.",Toast.LENGTH_SHORT).show(); return; }
        String clean=text.replace("♡","").replace("*","");
        tts.speak(clean,TextToSpeech.QUEUE_FLUSH,null,"anya_reply");
    }

    private void showVisualLab(){
        voiceConversation=false;
        base("Visual Lab");
        LinearLayout hero=card();
        hero.addView(portrait(ViewGroup.LayoutParams.MATCH_PARENT,dp(290)));
        hero.addView(text("Anya Visual Identity",20,TEXT,true));
        hero.addView(text("Use the same master face/reference image in each generator. The app keeps the prompt ready; each website controls its own generation rules.",13,MUTED,false));
        content.addView(hero);

        content.addView(section("Create with"));
        content.addView(action("✦ Open Perchance Anya",v->openUrl(PERCHANCE_ANYA)),mt(8));
        content.addView(action("✦ Open FreeImageMaker",v->openUrl(FREE_IMAGE_MAKER)),mt(8));
        content.addView(action("✦ Open SnowArt",v->openUrl(SNOWART)),mt(8));

        content.addView(section("Identity prompt"));
        TextView p=text(masterVisualPrompt(),13,MUTED,false); p.setPadding(dp(12),dp(12),dp(12),dp(12)); p.setBackground(round(CARD,18)); content.addView(p);
        content.addView(secondary("Copy Anya master prompt",v->copyPrompt(masterVisualPrompt())),mt(8));
        content.addView(infoCard("FreeImageMaker setup","Reference image: Anya master face • Keep my face • Portrait 4:5 • Seed 5 • 2 images."));
        content.addView(infoCard("Tip","Keep the identity block fixed and change only the final scene/outfit sentence for better consistency."));
    }

    private String masterVisualPrompt(){
        return "Same Anya as the reference image, preserve the exact same face and identity. Preserve the same eye shape, nose, lips, complexion, hairline, face shape and overall appearance. Long black hair, warm brown skin, expressive dark-brown eyes, elegant adult Tamil woman, photorealistic, realistic skin texture, high facial detail, natural anatomy.";
    }

    private void copyPrompt(String value){
        ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("Anya visual prompt",value));
        Toast.makeText(this,"Anya prompt copied.",Toast.LENGTH_SHORT).show();
    }

    private void openUrl(String url){
        try{ startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch(Exception e){ Toast.makeText(this,"Could not open website.",Toast.LENGTH_SHORT).show(); }
    }

    private void showSettings(){
        voiceConversation=false;
        base("More");
        LinearLayout preview=card(); preview.addView(portrait(ViewGroup.LayoutParams.MATCH_PARENT,dp(230))); content.addView(preview);
        content.addView(section("AI connection"));

        EditText key=new EditText(this); key.setHint("OpenRouter API key"); key.setHintTextColor(MUTED); key.setTextColor(TEXT); key.setText(prefs.getString("openrouter_key","")); key.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); key.setPadding(dp(14),dp(12),dp(14),dp(12)); key.setBackground(round(CARD,18)); content.addView(key,mt(8));
        EditText model=new EditText(this); model.setHint("Model"); model.setHintTextColor(MUTED); model.setTextColor(TEXT); model.setSingleLine(true); model.setText(prefs.getString("openrouter_model","openrouter/free")); model.setPadding(dp(14),dp(12),dp(14),dp(12)); model.setBackground(round(CARD,18)); content.addView(model,mt(8));
        content.addView(action("Save AI settings",v->{ prefs.edit().putString("openrouter_key",key.getText().toString().trim()).putString("openrouter_model",model.getText().toString().trim().isEmpty()?"openrouter/free":model.getText().toString().trim()).apply(); Toast.makeText(this,"AI settings saved locally.",Toast.LENGTH_SHORT).show(); }),mt(8));

        content.addView(section("Personality")); content.addView(infoCard("Style","Affectionate • playful • romantic • expressive • Tamil/Tanglish aware"));
        content.addView(section("Visuals")); content.addView(infoCard("Visual Lab","Perchance • FreeImageMaker • SnowArt"));
        content.addView(section("Voice")); content.addView(infoCard("Speech input","Android speech recognition")); content.addView(infoCard("Speech output","Android text-to-speech"));
        content.addView(section("Build")); content.addView(infoCard("Version","1.4.1 Visual Lab"));
    }

    private void scrollBottom(){ if(scrollView!=null) scrollView.postDelayed(()->scrollView.fullScroll(View.FOCUS_DOWN),100); }

    private LinearLayout nav(){
        LinearLayout bar=new LinearLayout(this); bar.setOrientation(LinearLayout.HORIZONTAL); bar.setPadding(dp(2),dp(5),dp(2),dp(5)); bar.setBackground(round(Color.rgb(27,21,33),22));
        bar.addView(navBtn("Home",v->showHome()),navWeight());
        bar.addView(navBtn("Chat",v->showChat()),navWeight());
        bar.addView(navBtn("Visuals",v->showVisualLab()),navWeight());
        bar.addView(navBtn("Call",v->showCall(true)),navWeight());
        bar.addView(navBtn("More",v->showSettings()),navWeight());
        return bar;
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
