import json
import matplotlib.pyplot as plt

def plot_tsp():
    import os

    # Try to find the result file
    result_file = 'target/tsp_result.json'
    if not os.path.exists(result_file):
        result_file = 'tsp_result.json'
    
    try:
        with open(result_file, 'r') as f:
            data = json.load(f)
        print(f"Loaded data from {result_file}")
    except FileNotFoundError:
        print("Error: tsp_result.json not found in 'target/' or current directory.")
        print("Please run the Java application first.")
        return

    cities = data['cities']
    route = data['route']
    distance = data['distance']

    # Extract coordinates
    x = [city['x'] for city in cities]
    y = [city['y'] for city in cities]

    # Reorder cities based on the route
    ordered_x = [cities[i]['x'] for i in route]
    ordered_y = [cities[i]['y'] for i in route]
    
    # Add the first city at the end to close the loop
    ordered_x.append(ordered_x[0])
    ordered_y.append(ordered_y[0])

    plt.figure(figsize=(10, 6))
    
    # Plot path
    plt.plot(ordered_x, ordered_y, 'b-', linewidth=1, label='Route')
    
    # Plot cities
    plt.scatter(x, y, c='red', s=50, zorder=5, label='Cities')
    
    # Annotate cities with their index
    for i, (cx, cy) in enumerate(zip(x, y)):
        plt.annotate(str(i), (cx, cy), xytext=(5, 5), textcoords='offset points')

    plt.title(f'TSP Best Route (Distance: {distance:.4f})')
    plt.xlabel('X Coordinate')
    plt.ylabel('Y Coordinate')
    plt.legend()
    plt.grid(True)
    
    # Ensure target directory exists for output if using target
    output_file = 'target/tsp_plot.png'
    if not os.path.exists('target'):
        os.makedirs('target')
        
    plt.savefig(output_file)
    print(f"Plot saved to {output_file}")
    plt.show() # Uncomment to show the plot window

if __name__ == "__main__":
    plot_tsp()
