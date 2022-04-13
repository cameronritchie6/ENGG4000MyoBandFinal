using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class OpenHighScore : MonoBehaviour
{
    public void HighScores(){
		SceneManager.LoadScene("HighScores");
	}
}
