using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class QJScore : MonoBehaviour
{
	//public GameObject textO;
	private TextMesh text;
	private string s;
	private string temp;
	private int score = 0;
	private float time = 0f;
	private float interval = 0.2f;
	
    // Start is called before the first frame update
    void Start()
    {
		s = "Score: 0";
        text = GetComponent<TextMesh>();
    }

    // Update is called once per frame
    void Update()
    {
		time = time + Time.deltaTime;
        text.text = s;
		if(time > interval){
			score++;
			time = time - interval;
		}
		temp = score.ToString();
		s = string.Format("Score: "+ temp);
    }
	
	public void bonusScore(){
		score = score + 100;
	}
	
	public void saveScore(){
		
		if(score > PlayerPrefs.GetInt("highscore")){
			PlayerPrefs.SetInt("highscore", score);
		}
	}
}
