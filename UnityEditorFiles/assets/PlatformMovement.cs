using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class PlatformMovement : MonoBehaviour
{
    private Rigidbody2D Rigid;
    private Vector2 Vector2 = Vector2.zero;
	private float T = 0.0f;
	private float waitT = 25.0f;
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
    
}
