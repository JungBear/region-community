import logo from './logo.svg';
import './App.css';
import { useEffect, useState } from 'react';


function App() {

  const [ data, setData ] = useState("");

  useEffect(()=>{
    const fetchData = async ()=>{
      const res = await fetch("/hello",{
          method:"GET",
          headers:{
            "Content-Type" : "application/json"
          }
      }
      );
      console.log(res);
      setData( await res.text());
    }

    fetchData();
  
  },[])

  return (
    <div className="App">
      {data}    
      hello
    </div>
  );
}

export default App;
