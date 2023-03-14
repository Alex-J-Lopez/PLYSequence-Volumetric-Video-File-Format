using System;
using System.IO;
using UnityEngine;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

public class PLYSequenceReader : MonoBehaviour
{
    public string plyFile;

    public float
        frameRate; // Desired frame rate. This will be recalculated into frame time later for invoke repeating.

    private StreamReader reader;
    private Mesh mesh;

    void Start()
    {
        // Check if the ply File is null before attempting to open it.
        if (plyFile != null)
        {
            reader = new StreamReader(plyFile);
            InvokeRepeating("renderFrame", 0f,
                1 / frameRate); // This invoke repeating method runs once per frame time amount of time.
        }
        else
        {
            Debug.LogError("PLY file is not assigned");
        }
    }

    /*
    This method gets vertex and color information from the ".plys" file and 
    assigns the data to the verticies and colors List objects.
    */
    private void renderFrame()
    {
        string line = reader.ReadLine();
        if (!line.Equals("ply"))
        {
            throw new FormatException("Header not found.");
        }

        List<string> header = new List<string>();
        while (!line.Equals("end_header"))
        {
            line = reader.ReadLine();
            header.Add(line);
        }

        int vertexCount = int.Parse(header.Find(s => s.StartsWith("element vertex")).Split(" ")[2]);

        List<Vector3> vertices = new List<Vector3>(vertexCount);
        List<Color> colors = new List<Color>(vertexCount);

        switch (header.Find(s => s.StartsWith("format")))
        {
            case "format ascii 1.0":
                List<string> lines = new List<string>(vertexCount);
                for (int i = 0; i < vertexCount; i++)
                {
                    lines.Add(reader.ReadLine());
                }

                foreach (string vertex in lines.AsParallel())
                {
                    string[] lineContents = vertex.Split(" ");

                    /*
                Get the xyz components of the vertice and add it to the List of vertices.
                */
                    float x = float.Parse(lineContents[0]);
                    float y = float.Parse(lineContents[1]);
                    float z = float.Parse(lineContents[2]);

                    /*
                    Now we grab the color from the file.
                    */
                    float r = float.Parse(lineContents[3]);
                    float g = float.Parse(lineContents[4]);
                    float b = float.Parse(lineContents[5]);
                    // Divide each color component by 255 to allow the Color constructor to parse correctly.

                    vertices.Add(new Vector3(x, y,
                        z)); //Add the new Vector3 that describes the point in space to the vector Array.
                    colors.Add(new Color(r / 255f, g / 255f, b / 255f));
                }

                reader.ReadLine();
                break;
            case "format binary_little_endian 1.0":
                throw new Exception("TODO"); //todo
            case "format binary_big_endian 1.0":
                throw new Exception("TODO"); //todo
            default:
                throw new FormatException("Could not find file format.");
        }

        int[] indices = new int[vertexCount];
        Parallel.For(0, vertexCount, i => indices[i] = i);

        Destroy(mesh);
        mesh = new Mesh();
        mesh.vertices = vertices.ToArray();
        mesh.colors = colors.ToArray();
        mesh.SetIndices(indices, MeshTopology.Points, 0);
        GetComponent<MeshFilter>().mesh = mesh;
    }
}