using System;
using System.IO;
using UnityEngine;
using System.Collections;
using System.Collections.Generic;

public class PLYSequenceReader : MonoBehaviour
{
    public string plyFile;

    public float
        frameRate; // Desired frame rate. This will be recalculated into frame time later for invoke repeating.

    private int vertexCount;
    private StreamReader reader;
    private List<Vector3> vertices = new List<Vector3>();
    private List<Color> colors = new List<Color>();
    private Mesh mesh;
    private List<string> header;

    void Start()
    {
        float frameTime = 1 / frameRate; // Calculate frame time.
        // Check if the ply File is null before attempting to open it.
        if (plyFile != null)
        {
            reader = new StreamReader(plyFile);
            InvokeRepeating("methodRunner", 0f,
                frameTime); // This invoke repeating method runs once per frame time amount of time.
        }
        else
        {
            Debug.LogError("PLY file is not assigned");
        }
    }

    /*
    This method is responsible for running the get vertex and color method followed by generate mesh 
    in order to update the mesh once every time the method is called. This method is called by invoke repeating 
    to simulate a video format.
    */
    private void methodRunner()
    {
        GetVertexAndColor();
        GenerateMeshFromContents();
    }

    /*
    This method gets vertex and color information from the ".plys" file and 
    assigns the data to the verticies and colors List objects.
    */
    private void GetVertexAndColor()
    {
        string line = reader.ReadLine();
        if (!line.Equals("ply"))
        {
            throw new FormatException("Header not found.");
        }

        header = new List<string>();
        while (!line.Equals("end_header"))
        {
            line = reader.ReadLine();
            header.Add(line);
        }

        vertexCount = int.Parse(header.Find(s => s.StartsWith("element vertex")).Split(" ")[2]);

        for (int i = 0; i < vertexCount; i++)
        {
            string currentLine = reader.ReadLine();
            string[] lineContents = currentLine.Split(" ");
            /*
            Get the xyz components of the vertice and add it to the List of vertices.
            */
            float x = float.Parse(lineContents[0]);
            float y = float.Parse(lineContents[1]);
            float z = float.Parse(lineContents[2]);
            vertices.Add(new Vector3(x, y,
                z)); //Add the new Vector3 that describes the point in space to the vector Array.

            /*
            Now we grab the color from the file.
            */
            float r = float.Parse(lineContents[3]);
            float g = float.Parse(lineContents[4]);
            float b = float.Parse(lineContents[5]);
            // Divide each color component by 255 to allow the Color constructor to parse correctly.
            colors.Add(new Color(r / 255f, g / 255f, b / 255f));
        }
    }

    /*
    This method updates the mesh component of the game object that this script is attached 
    to. Color and vertex count will be updated upon completion.
    */
    private void GenerateMeshFromContents()
    {
        int[] indices = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++)
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
        vertices.Clear(); // Clear the vertices List to allow new frame to be written.
        colors.Clear();
    }
}