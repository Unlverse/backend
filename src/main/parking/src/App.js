import {useEffect, useState} from "react";
import axios from "axios";

function App() {
  const [hello, setHello] = useState('');
  const [error, setError] = useState('');
  useEffect(() => {
    axios.get('http://localhost:8080/api/test')
        .then((res) => {
          setHello(res.data);
        })
        .catch((err) => {
          setError(err.message);
        });
  }, []);

  return (
      <div className="App">
        백엔드에서 받은 데이터: {hello}
        {error && <p>Error: {error}</p>}
      </div>
  );
}

export default App;
