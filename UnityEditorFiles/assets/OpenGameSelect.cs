using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class OpenGameSelect : MonoBehaviour
{
    public void GameSelect(){
		SceneManager.LoadScene("GameSelect");
	}
	public void Quit(){
		UnityEditor.EditorApplication.isPlaying = false;
	}
}
