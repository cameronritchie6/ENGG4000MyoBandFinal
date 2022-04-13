using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class HighScores : MonoBehaviour
{
	
	private int qJHighScore;
	public TMPro.TextMeshProUGUI qJText;
	private int SGLHighScore;
	public TMPro.TextMeshProUGUI SGLText;
	private int SGHighScore;
	public TMPro.TextMeshProUGUI SGText;
	
    // Start is called before the first frame update
    void Start()
    {
       qJHighScore = PlayerPrefs.GetInt("highscore"); 
	   qJText.text = $"Geojump Score: {qJHighScore}";
	   SGLHighScore = PlayerPrefs.GetInt("SGLhighscore"); 
	   SGLText.text = $"Void Fighter Lite Score: {SGLHighScore}";
	   SGHighScore = PlayerPrefs.GetInt("SGhighscore");
	   SGText.text = $"Void Fighter Score: {SGHighScore}";
    }

   
}
