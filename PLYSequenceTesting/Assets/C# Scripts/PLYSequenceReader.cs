using System.IO;
using UnityEngine;
using System.Collections;
using System.Collections.Generic;

public class PLYSequenceReader : MonoBehaviour
{
    public TextAsset plyFile;
    private int vertexCount;
    private StringReader reader;
    private List<Vector3> vertices = new List<Vector3>();
    private List<Color> colors = new List<Color>();
    private Mesh mesh;

    void Start()
    {
        if (plyFile != null)
        {
            reader = new StringReader(plyFile.text);
            InvokeRepeating("methodRunner", 0f, 0.08f);
        }
        else
        {
            Debug.LogError("PLY file is not assigned");
        }
    }

    private void methodRunner()
    {
        GetVertexAndColor();
        GenerateMeshFromContents();
    }

    private void GetVertexAndColor()
    {
        reader.ReadLine();
        reader.ReadLine();
        string fullLine = reader.ReadLine();
        string[] line = fullLine.Split(" ");
        vertexCount = int.Parse(line[2]);
        Debug.Log(vertexCount);
        reader.ReadLine();
        reader.ReadLine();
        reader.ReadLine();
        reader.ReadLine();
        reader.ReadLine();
        reader.ReadLine();
        reader.ReadLine();
        /*
        7 read lines dumps the header because we are 
        guranteeing that there will be these described components in the file.
        */
        for (int i = 0; i < vertexCount; i++) //Added minus one just in case there might be index out of bounds error.
        {
            string currentLine = reader.ReadLine();
            string[] lineContents = currentLine.Split(" ");
            /*
            Get the xyz components of the vertice and add it to the List of vertices.
            */
            float x = float.Parse(lineContents[0]);
            float y = float.Parse(lineContents[1]);
            float z = float.Parse(lineContents[2]);
            vertices.Add(new Vector3(x, y, z)); //Add the new Vector3 that describes the point in space to the vector Array.
            
            /*
            Now we grab the color from the file.
            */
            float r = float.Parse(lineContents[3]);
            float g = float.Parse(lineContents[4]);
            float b = float.Parse(lineContents[5]);
            colors.Add(new Color(r/255f, g/255f, b/255f));
        }
    }

    private void GenerateMeshFromContents()
    {
        int[] indices = new int[vertexCount];
        for(int i = 0; i<vertexCount; i++)
        {
            indices[i] = i;
        }
        if (mesh != null)
        {
            Destroy(mesh);
        }
        mesh = new Mesh();
        mesh.vertices = vertices.ToArray();
        mesh.colors = colors.ToArray();
        mesh.SetIndices(indices, MeshTopology.Points, 0);
        GetComponent<MeshFilter>().mesh = mesh;
        vertices = new List<Vector3>();
        colors = new List<Color>();
    }
}