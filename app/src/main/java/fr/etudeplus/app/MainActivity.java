package fr.etudeplus.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import android.speech.tts.TextToSpeech;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout root, panel; TextToSpeech voice; String level="Découverte", subject="Français"; int index=0, success=0;
    final int navy=Color.rgb(33,58,72), teal=Color.rgb(27,117,111), cream=Color.rgb(250,248,241);
    final String[][][] tasks={
      {{"Associe le mot à sa catégorie", "Pomme", "Fruit", "Animal", "Fruit"},{"Complète : Le chat ___", "miaule", "miaule", "vole", "miaule"},{"Choisis le bon mot", "Une maison", "Habitation", "Véhicule", "Habitation"}},
      {{"Combien font 3 + 4 ?", "7", "7", "9", "7"},{"Combien font 5 × 2 ?", "10", "10", "12", "10"},{"Quel nombre est le plus grand ?", "18", "18", "8", "18"}},
      {{"Quelle partie de la plante absorbe l'eau ?", "Racines", "Racines", "Feuilles", "Racines"},{"Quel astre éclaire la Terre ?", "Soleil", "Soleil", "Lune", "Soleil"},{"L'eau gelée s'appelle...", "Glace", "Glace", "Vapeur", "Glace"}}
    };
    @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(navy);voice=new TextToSpeech(this,status->{if(status==TextToSpeech.SUCCESS)voice.setLanguage(Locale.FRENCH);}); home();}
    GradientDrawable bg(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(26);return d;}
    TextView text(String s,int sp,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(navy);t.setPadding(14,12,14,12);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    void init(String heading){ScrollView sc=new ScrollView(this);sc.setFillViewport(true);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(22,24,22,28);root.setBackgroundColor(cream);sc.addView(root);setContentView(sc);root.addView(text("🌱 Étude+",28,true));root.addView(text(heading,23,true));}
    void button(String s,Runnable action){TextView b=text(s,21,true);b.setGravity(Gravity.CENTER);b.setTextColor(Color.WHITE);b.setBackground(bg(teal));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,9,0,9);b.setMinHeight(66);root.addView(b,p);b.setOnClickListener(v->action.run());}
    void say(String s){if(voice!=null)voice.speak(s,TextToSpeech.QUEUE_FLUSH,null,"etude");}
    void home(){init("Bienvenue ! Apprenons ensemble, à ton rythme.");root.addView(text("Choisis ton niveau. Tu peux le changer à tout moment.",19,false));for(String s:new String[]{"Découverte","Primaire","Collège","Adulte"})button(s,()->{level=s;subjects();});root.addView(text("Aucune publicité. Pas de compte nécessaire. Les résultats restent sur cet appareil.",16,false));}
    void subjects(){init("Niveau : "+level);root.addView(text("Choisis une matière",20,false));for(String s:new String[]{"Français","Mathématiques","Sciences"})button(s,()->{subject=s;index=0;success=0;exercise();});button("← Changer de niveau",this::home);}
    int sub(){return subject.equals("Français")?0:subject.equals("Mathématiques")?1:2;}
    void exercise(){init(subject+" · "+level);String[] t=tasks[sub()][index];root.addView(text("Exercice "+(index+1)+" sur 3",18,false));root.addView(text(t[0],24,true));button("🔊 Écouter la consigne",()->say(t[0]));root.addView(text("Glisse une réponse sur la zone, ou touche une réponse.",17,false));TextView target=text("Dépose ta réponse ici",20,true);target.setGravity(Gravity.CENTER);target.setMinHeight(100);target.setBackground(bg(Color.rgb(225,239,234)));root.addView(target);final boolean[] done={false};java.util.function.Consumer<String> check=(answer)->{if(done[0])return;if(answer.equals(t[4])){done[0]=true;success++;target.setText("✓ Bravo ! "+answer);target.announceForAccessibility("Bonne réponse. Bravo !");say("Bravo ! Bonne réponse.");button(index==2?"Voir mon résultat →":"Exercice suivant →",()->{if(index==2)result();else{index++;exercise();}});}else{target.setText("Essaie encore, tu peux y arriver.");say("Essaie encore. Prends ton temps.");}};
      for(String answer:new String[]{t[2],t[3]}){TextView option=text("☰  "+answer,21,true);option.setMinHeight(66);option.setGravity(Gravity.CENTER);option.setBackground(bg(Color.WHITE));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,8,0,8);root.addView(option,p);option.setOnClickListener(v->check.accept(answer));option.setOnLongClickListener(v->{android.content.ClipData clip=android.content.ClipData.newPlainText("réponse",answer);v.startDragAndDrop(clip,new View.DragShadowBuilder(v),null,0);return true;});}
      target.setOnDragListener((v,e)->{if(e.getAction()==DragEvent.ACTION_DROP){if(e.getClipData()!=null)check.accept(e.getClipData().getItemAt(0).getText().toString());return true;}return true;});button("← Matières",this::subjects);
    }
    void result(){getPreferences(0).edit().putInt("best_"+level+subject,Math.max(success,getPreferences(0).getInt("best_"+level+subject,0))).apply();init("Bien joué ! 🎉");root.addView(text("Tu as réussi "+success+" réponses sur 3.",23,true));root.addView(text("Chaque essai compte. Tu peux recommencer autant que tu veux.",19,false));button("Recommencer",()->{index=0;success=0;exercise();});button("Changer de matière",()->{success=0;subjects();});button("Accueil",()->{success=0;home();});}
    @Override protected void onDestroy(){if(voice!=null){voice.stop();voice.shutdown();}super.onDestroy();}
}