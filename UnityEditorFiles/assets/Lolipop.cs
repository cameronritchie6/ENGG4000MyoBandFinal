using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Lolipop : MonoBehaviour
{
	private GameObject text;
	private GameObject[] findText;
	
	
    void OnTriggerEnter2D(){
		findText = GameObject.FindGameObjectsWithTag("Score");
		text = findText[0];
		text.SendMessage("bonusScore");
		Destroy(gameObject);
	}
}
