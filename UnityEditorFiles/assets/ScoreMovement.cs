using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ScoreMovement : MonoBehaviour
{
	public GameObject Player;
	private Vector2 playerPos;
	private float oldPlayerPosY = 0.52f;
	private float playerPosY = 0.52f;
	private float playerPosYDiff = 0.0f;
    // Start is called before the first frame update
    void Start()
    {
		
        playerPos = Player.transform.position;
		playerPosY = playerPos.y;
		playerPosYDiff = playerPosY - oldPlayerPosY;
		transform.position = new Vector2(-7.12f, 4f + playerPosYDiff);
		
    }

    // Update is called once per frame
    void Update()
    {
        
        playerPos = Player.transform.position;
		playerPosY = playerPos.y;
		playerPosYDiff = playerPosY - oldPlayerPosY;
		transform.position = new Vector2(-7.12f, 4f + playerPosYDiff);
    }
}
