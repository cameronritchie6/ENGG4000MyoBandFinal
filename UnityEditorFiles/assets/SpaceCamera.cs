using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class SpaceCamera : MonoBehaviour
{
	public GameObject Player;
	private Vector2 PP;
	private Vector2 PPx;
	private Rigidbody2D Rigid;
	private Vector2 Vector2 = Vector2.zero;
	private bool Boss = false;
	
    // Start is called before the first frame update
    void Start()
    {
        Rigid = GetComponent<Rigidbody2D>();
		Vector2 = Vector2.left;
		
    }

    // Update is called once per frame
    void Update()
    {
		if(Boss == false){
			PP = Player.transform.position;
			PPx = new Vector2(PP.x, 0);
			Rigid.MovePosition(new Vector2(7,0) + PPx);
		}
    }
	void OnCollisionEnter2D(Collision2D collision) { 
        if(collision.gameObject.tag == "CameraStop"){
			Boss = true;
		}
    }
}
