using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class SpikeMovement : MonoBehaviour
{
	private Rigidbody2D Rigid;
	private Vector2 Vector2 = Vector2.zero;
	private float T = 0.0f;
	private float waitT = 25.0f;
	
	private GameObject text;
	private GameObject[] findText;
	
    // Start is called before the first frame update
    void Start()
    {
        Rigid = GetComponent<Rigidbody2D>();
		Vector2 = Vector2.left;
		
    }

    // Update is called once per frame
    void Update()
    {
		T += Time.deltaTime;
        Rigid.MovePosition(Rigid.position + (Vector2 * 4) * Time.fixedDeltaTime);
		if (T > waitT){
			Destroy(gameObject);
		}
		
    }
    void OnCollisionEnter2D(Collision2D col) { 
        findText = GameObject.FindGameObjectsWithTag("Score");
		text = findText[0];
		text.SendMessage("saveScore");
		SceneManager.LoadScene("GameOverQJ");
    }
	
}
