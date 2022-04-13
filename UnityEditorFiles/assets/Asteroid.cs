using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class Asteroid : MonoBehaviour
{
	private GameObject text;
	private GameObject[] findText;
	
    void OnCollisionEnter2D(Collision2D collision) { 
        if(collision.gameObject.name == "PlayerShipLite"){
			findText = GameObject.FindGameObjectsWithTag("Score");
			text = findText[0];
			text.SendMessage("saveScore");
			SceneManager.LoadScene("GameOverSGL");
		}
		else if(collision.gameObject.name == "PlayerShip"){
			findText = GameObject.FindGameObjectsWithTag("Score");
			text = findText[0];
			text.SendMessage("saveScore");
			SceneManager.LoadScene("GameOverSG");
		}
    }
}
