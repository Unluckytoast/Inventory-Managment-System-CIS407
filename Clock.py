import tkinter as tk  # Import the tkinter library
from time import strftime  # Import strftime to format the current time

root = tk.Tk() 
root.title("Clock")  # Set the title of the window
root.geometry('400x78')  #Sets the size of the window


def time():
    # Format the current time in hours, minutes, seconds, and AM/PM
    time_format = strftime('%H:%M:%S %p')# Formats time
    lbl.config(text=time_format)  # Updates with the current time
    lbl.after(1000, time)  # Calls function again after 1 second)
# Creates widget that display the time
lbl = tk.Label(root,
               font=('georgia', 45, 'bold'),  # Set font type, size, and style
               background='white',
               foreground='black')  
lbl.grid(row=0, column=0)
time()  

root.mainloop()
