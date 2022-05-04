using System.Collections;
using System.Collections.Generic;
using UnityEngine;


public class TheAdministratorScript : MonoBehaviour
{
	
	private float xS = 58.885f;
	private float yS = 1.77875f;
	private float zS = 0.0f;
	private int   gO = 1;
	private float T = 6.0f;
	private float waitT = 7.0f;
	
	public GameObject Section1;
	public GameObject Section2;
	public GameObject Section3;
	public GameObject Section4;
	public GameObject Section5;
	public GameObject Section6;
	
    
	
	
	void CreateSection(){
		gO = UnityEngine.Random.Range(1,7);
		if(gO>=1 && gO<2){
			Instantiate(Section1, new Vector3(xS,yS,zS), Quaternion.identity);
		}
		else if(gO>=2 && gO<3){ 
			Instantiate(Section2, new Vector3(xS,yS,zS), Quaternion.identity);
		}
		else if(gO>=3 && gO<4){ 
			Instantiate(Section3, new Vector3(xS,yS,zS), Quaternion.identity);
		}
		else if(gO>-4 && gO<5){ 
			Instantiate(Section4, new Vector3(xS,yS,zS), Quaternion.identity);
		}
		else if(gO>-5 && gO<6){ 
			Instantiate(Section5, new Vector3(xS,yS,zS), Quaternion.identity);
		}
		else{ 
			Instantiate(Section6, new Vector3(xS,yS,zS), Quaternion.identity);
		}
	}
	void CreateSectionTest(){
		Instantiate(Section6, new Vector3(xS,yS,zS), Quaternion.identity);
	}
    // Update is called once per frame
    void Update()
    {
		T += Time.deltaTime;
		//if(T==300){
			//Invoke("CreateBlock", 0.4f);
			//T = 1;
		//}
		if(T > waitT){
			Invoke("CreateSection",0.4f);
			//Invoke("CreateSectionTest",0.4f);
			T = T - waitT;
			
		}
    }
}
