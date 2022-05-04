using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class SpaceGameLiteScore : MonoBehaviour
{
    private TMPro.TextMeshProUGUI text;
	private string s;
	private string temp;
	private int score = 0;
	
	
    // Start is called before the first frame update
    void Start()
    {
		s = "Score: 0";
        text = GetComponent<TMPro.TextMeshProUGUI>();
    }

    // Update is called once per frame
   
	
	public void bonusScore(){
		score = score + 100;
		temp = score.ToString();
		s = string.Format("Score: "+ temp);
		text.text = s;
	}
	
	
	public void penalty(){
		score = score - 50;
		temp = score.ToString();
		s = string.Format("Score: "+ temp);
		text.text = s;
	}
	
	public void saveScore(){
		
		if(score > PlayerPrefs.GetInt("SGLhighscore")){
			PlayerPrefs.SetInt("SGLhighscore", score);
			PlayerPrefs.Save();
		}
	}
}
