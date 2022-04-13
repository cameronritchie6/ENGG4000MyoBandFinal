using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class OrangeTorpedo : MonoBehaviour
{
    private Rigidbody2D Rigid;
	private Vector2 Vector2 = Vector2.zero;
	private float T = 0.0f;
	private float WaitT = 2.0f;
	
	
    // Awake is called when the object is instantiated
    private void Awake()
    {
        Rigid = GetComponent<Rigidbody2D>();
		Vector2 = Vector2.left;
		
    }

    // Update is called once per frame
    void Update()
    {
		T += Time.deltaTime;
		if(T > WaitT){
			Destroy(gameObject);
		}
        Rigid.MovePosition(Rigid.position + (Vector2 * (-16)) * Time.fixedDeltaTime);
		
    }
	void OnTriggerEnter2D(Collider2D collision) { 
        
			Destroy(gameObject);
		
    }
	
	void OnCollisionEnter2D(Collision2D collision) { 
        
			Destroy(gameObject);
		
    }
}
