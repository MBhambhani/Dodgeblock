package com.example.dodgeblock;

import com.example.dodgeblock.GameActivity;
import com.example.dodgeblock.InstructionsActivity;
import com.example.dodgeblock.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

private TextView play, instr;
	
	
	
	/**Called when the app is started*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final Activity context = this;
		
		play = (TextView)findViewById(R.id.Play);
		instr = (TextView)findViewById(R.id.Instr);
		
		play.setOnClickListener(new View.OnClickListener() {
			
			/**Click event for the 'Play' TextView*/
			@Override
			public void onClick(View v) {
				Intent game = new Intent(context, GameActivity.class);
				game.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(game);
			}
		});
		
		instr.setOnClickListener(new View.OnClickListener() {
			
			/**Click event for the 'Instructions' TextView*/
			@Override
			public void onClick(View v) {
				Intent instructions = new Intent(context, InstructionsActivity.class);
				instructions.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(instructions);
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
