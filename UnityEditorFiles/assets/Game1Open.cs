using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class Game1Open : MonoBehaviour
{
    public void QuadJumpOpen(){
		SceneManager.LoadScene("Quadrilateral Jump");
	}
	public void SGOpen(){
		SceneManager.LoadScene("SpaceGame");
	}
	public void SGLOpen(){
		SceneManager.LoadScene("SpaceGameLite");
	}
	public void Back(){
		SceneManager.LoadScene("MainMenu");
	}
}
